package com.hanium.healthband;

import android.graphics.Color;
import android.util.Log;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class GenerateData<T> {

    public LineData generateLineData(ArrayList<Entry> entries) {

        LineData d = new LineData();


        LineDataSet set = new LineDataSet(entries, "평균");
        set.setColor(Color.rgb(240, 238, 70));
        set.setLineWidth(2.5f);
        set.setCircleColor(Color.rgb(240, 238, 70));
        set.setCircleRadius(5f);
        set.setFillColor(Color.rgb(240, 238, 70));
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setDrawValues(true);
        set.setValueTextSize(10f);
        set.setValueTextColor(Color.rgb(240, 238, 70));

        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        d.addDataSet(set);

        return d;
    }

    public CandleData generateCandleData(ArrayList<CandleEntry> entries, String label) {

        CandleData d = new CandleData();

        for(int i = 0; i < entries.size(); i++){
            Log.d("gene" , entries.get(i).getHigh() + " " + entries.get(i).getLow() +
                                        " " + entries.get(i).getOpen() + " " + entries.get(i).getLow());
        }
        CandleDataSet set = new CandleDataSet(entries, label);
        set.setShadowWidth(0f);
        set.setDecreasingColor(Color.rgb(142, 150, 175));
        set.setShadowColor(Color.DKGRAY);
        set.setBarSpace(0.3f);
        set.setValueTextSize(10f);
        set.setDrawValues(false);
        d.addDataSet(set);

        return d;
    }
}
