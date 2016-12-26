package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

    public static boolean showPercent = true;
    private static String LOG_TAG = Utils.class.getSimpleName();

    public static ArrayList quoteJsonToContentVals(final Context context, String JSON, boolean asJSONObjects) {
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;
        try {
            jsonObject = new JSONObject(JSON);
            if (jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));
                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    if (!(jsonObject.getString("Bid") == null || jsonObject.getString("Bid").equals("null"))) {
                        try {
                            Intent intent = new Intent(MyStocksActivity.ACTION_STOCK_FOUND);
                            intent.putExtra("name", jsonObject.getString("symbol"));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (!asJSONObjects) {
                            batchOperations.add(buildBatchOperation(jsonObject));
                        } else {
                            jsonObjects.add(jsonObject);
                        }

                    } else {
                        try {
                            Intent intent = new Intent(MyStocksActivity.ACTION_STOCK_NOT_FOUND);
                            intent.putExtra("name", jsonObject.getString("symbol"));
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            if (!(jsonObject.getString("Bid") == null || jsonObject.getString("Bid").equals("null"))) {
                                batchOperations.add(buildBatchOperation(jsonObject));
                            } else {
                                try {
                                    Intent intent = new Intent(MyStocksActivity.ACTION_STOCK_NOT_FOUND);
                                    intent.putExtra("name", jsonObject.getString("symbol"));
                                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
        }
        return asJSONObjects ? jsonObjects : batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        if (bidPrice.equalsIgnoreCase(null)) {
            bidPrice = String.valueOf(0);
        }
        bidPrice = String.format(Locale.US, "%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        if (change.equalsIgnoreCase(null)) {
            change = String.valueOf(0);
        }
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.US, "%.2f", round);
        StringBuffer changeBuffer = new StringBuffer(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }
            builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
            builder.withValue(QuoteColumns.DAYSHIGH, jsonObject.getString("DaysHigh"));
            builder.withValue(QuoteColumns.DAYSLOW, jsonObject.getString("DaysLow"));
            builder.withValue(QuoteColumns.YEARHIGH, jsonObject.getString("YearHigh"));
            builder.withValue(QuoteColumns.YEARLOW, jsonObject.getString("YearLow"));
            builder.withValue(QuoteColumns.LASTTRADE, jsonObject.getString("LastTradePriceOnly"));
            builder.withValue(QuoteColumns.TODAYRANGE, jsonObject.getString("DaysRange"));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }
}
