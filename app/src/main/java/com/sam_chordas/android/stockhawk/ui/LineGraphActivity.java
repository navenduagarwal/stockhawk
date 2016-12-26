package com.sam_chordas.android.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Activity managing graphs on click
 */

public class LineGraphActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public final static String EXTRA_SYMBOL = "linegraph_symbol";
    private static final int CURSOR_LOADER_ID = 0;

    private LineChartView mChart;
    private String mSymbol;
    private int mMaxValue = 0;
    private int mMinValue;
    private TextView mDetailTextView;

    public static Intent newIntent(Context context, String symbol) {
        Intent intent = new Intent(context, LineGraphActivity.class);
        intent.putExtra(EXTRA_SYMBOL, symbol);
        return intent;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);
        mChart = (LineChartView) findViewById(R.id.linechart);
        mDetailTextView = (TextView) findViewById(R.id.chart_detail);

        mSymbol = getIntent().getStringExtra(EXTRA_SYMBOL);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(mSymbol);
        }

        getSupportLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(this,
                QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE},
                QuoteColumns.SYMBOL + " = ?",
                new String[]{mSymbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        LineSet dataSet = setLineSet(data);
        if (dataSet != null && dataSet.size() != 0) {
            setChart(dataSet);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private LineSet setLineSet(Cursor cursor) {

        LineSet dataSet = new LineSet();

        cursor.moveToFirst();

        for (int i = 0; i < cursor.getCount(); i++) {
            int columnIndex = cursor.getColumnIndex(QuoteColumns.BIDPRICE);

            float bidPrice = cursor.getFloat(columnIndex);

            if (bidPrice > mMaxValue) {
                mMaxValue = Math.round(bidPrice);
            }

            if (i == 0) {
                mMinValue = Math.round(bidPrice);
            }

            if (bidPrice < mMinValue) {
                mMinValue = Math.round(bidPrice);
            }

            dataSet.addPoint(bidPrice + "", bidPrice);
            cursor.moveToNext();
        }

        dataSet.setColor(Color.parseColor("#53c1bd"))
                .setFill(Color.parseColor("#3d6c73"))
                .setGradientFill(new int[]{Color.parseColor("#364d5a"), Color.parseColor("#3f7178")}, null);

        return dataSet;
    }

    private void setChart(LineSet dataSet) {

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.parseColor("#53c1bd"));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(2));

        int min = (int) (mMaxValue / 1.3) - (int) ((mMaxValue / 1.3) % 100);
        int max = (100 - (int) (mMaxValue * 1.3) % 100) + (int) (mMaxValue * 1.3);
        int step = (max - min) / 5;

        mChart.setBorderSpacing(20)
                .setAxisBorderValues(min, max, step)
                .setLabelsColor(Color.parseColor("#3d6c73"))
                .setXAxis(false)
                .setYAxis(false)
                .setBorderSpacing(Tools.fromDpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);

        mChart.addData(dataSet);
        mChart.show();
        mDetailTextView.setText("Max Value: " + mMaxValue + "\n" + "Min Value: " + mMinValue);
    }
}
