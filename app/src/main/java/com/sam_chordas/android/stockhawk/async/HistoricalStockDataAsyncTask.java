package com.sam_chordas.android.stockhawk.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Async task to fetch historical data
 */

public class HistoricalStockDataAsyncTask extends AsyncTask<Void, Void, ArrayList<JSONObject>> {

    ArrayList<JSONObject> mContentVals;
    private String mSymbol;
    private String mStringResponse;
    private Context context;

    public HistoricalStockDataAsyncTask(Context context, String symbol) {
        this.context = context;
        mSymbol = symbol;
    }

    @Override
    protected void onPostExecute(ArrayList<JSONObject> contentVals) {
        super.onPostExecute(contentVals);

        ArrayList<String> endValues = new ArrayList<>();
        ArrayList<String> dates = new ArrayList<>();

        for (JSONObject jsonObject : mContentVals) {
            try {
                endValues.add(jsonObject.getString("Close"));
                dates.add(jsonObject.getString("Date"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Intent intent = new Intent(context, LineGraphActivity.class);
        intent.putStringArrayListExtra("endValues", endValues);
        intent.putStringArrayListExtra("dates", dates);
        context.startActivity(intent);
    }

    @Override
    protected ArrayList<JSONObject> doInBackground(Void... params) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + mSymbol + "/chartdata;type=quote;range=5y/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

        Response response;

        try {
            response = client.newCall(request).execute();
            mStringResponse = response.body().string();
            mContentVals = Utils.quoteJsonToContentVals(context, mStringResponse, true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mContentVals;
    }

}