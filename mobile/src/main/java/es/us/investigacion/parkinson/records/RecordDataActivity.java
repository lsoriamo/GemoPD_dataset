package es.us.investigacion.parkinson.records;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.us.investigacion.parkinson.R;

public class RecordDataActivity extends AppCompatActivity implements
        OnChartGestureListener, OnChartValueSelectedListener {

    public static final String RECORD_PATIENT = "record_patient";
    public static final String RECORD_DATE = "record_date";
    public static final String RECORD_ID = "record_id";
    public static final String RECORD_PARENT_ID = "record_parent_id";
    public static final String RECORD_FROM_SESSION_IN_PROGRESS = "from_session_in_progress";
    private SimpleDateFormat sDateFormat;
    private File file;

    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_data);
        sDateFormat = new SimpleDateFormat(getString(R.string.date_format_file));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RecordDataActivity.this.finish();
                }
            });
        }
        String patient = getIntent().getStringExtra(RECORD_PATIENT);
        Date start = new Date(getIntent().getLongExtra(RECORD_DATE, 0));
        file = new File(getExternalFilesDir(Environment.getDataDirectory().getAbsolutePath()).getAbsolutePath());
        file = new File(file.getAbsolutePath() + getString(R.string.file_folder) + patient.trim() + "/" + getString(R.string.file_prefix) + sDateFormat.format(start) + getString(R.string.file_extension));
        if (!file.exists()) {
            Toast.makeText(RecordDataActivity.this, getString(R.string.file_not_found), Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }

        mChart = (LineChart) findViewById(R.id.chart1);
        mChart.setOnChartGestureListener(this);
        mChart.setOnChartValueSelectedListener(this);
        mChart.setDrawGridBackground(false);

        // no description text
        mChart.setDescription("");
        mChart.setNoDataTextDescription(getString(R.string.no_data_available_session));

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        // mChart.setScaleXEnabled(true);
        // mChart.setScaleYEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(false);

        Typeface tf = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);

        YAxis leftAxis = mChart.getAxisLeft();
//        leftAxis.removeAllLimitLines();
//        leftAxis.addLimitLine(ll1);
        leftAxis.enableGridDashedLine(3f, 3f, 3f);
        leftAxis.setDrawZeroLine(true);
        leftAxis.setDrawLimitLinesBehindData(true);
        mChart.getAxisRight().setEnabled(false);
        Pair<Float, Float> minMaxValues = setData();
        leftAxis.setAxisMaxValue(minMaxValues.second + 1);
        leftAxis.setAxisMinValue(minMaxValues.first - 1);

        mChart.animateX(2500, Easing.EasingOption.EaseInOutQuart);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        l.setPosition(Legend.LegendPosition.BELOW_CHART_CENTER);
        l.setForm(Legend.LegendForm.LINE);

        mChart.setVisibleXRangeMaximum(100f);

        // mChart.setAutoScaleMinMaxEnabled(true);
        // mChart.invalidate();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    private Pair<Float, Float> setData() {

        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        List<ArrayList<Entry>> yVals = new ArrayList<ArrayList<Entry>>();
        List<String> xVals = new ArrayList<>();
        Map<Integer, String> events = new HashMap<>();
        BufferedReader reader = null;
        String eventLine = getString(R.string.event_value_prefix);
        String eventDelimiter = getString(R.string.delimiter);
        try {
            reader = new BufferedReader(new FileReader(file));
            String currentLine;
            int i = 0;
            boolean first = true;
            while ((currentLine = reader.readLine()) != null) {
                if (!currentLine.contains(eventLine)) {
                    String[] split = currentLine.split(",");
                    for (int elem = 0; elem < split.length; elem++) {
                        if (first) {
                            yVals.add(new ArrayList<Entry>());
                        }
                        float value = Float.parseFloat(split[elem]);
                        yVals.get(elem).add(new Entry(value, i));

                        if (value > maxValue)
                            maxValue = value;
                        if (value < minValue)
                            minValue = value;
                    }
                    i++;
                    xVals.add(Integer.toString(i));
                    first = false;
                } else {
                    String[] split = currentLine.split(eventDelimiter);
                    events.put(i, split[1]);
                }
            }
            LineDataSet set1 = getLineDataSet(yVals.get(0), Color.BLACK, Color.BLACK, Color.WHITE, "Acc X");
            LineDataSet set2 = getLineDataSet(yVals.get(1), Color.RED, Color.RED, Color.WHITE, "Acc Y");
            LineDataSet set3 = getLineDataSet(yVals.get(2), Color.BLUE, Color.BLUE, Color.WHITE, "Acc Z");
            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(set1);
            dataSets.add(set2);
            dataSets.add(set3);
            LineData data = new LineData(xVals, dataSets);
            mChart.setData(data);

            XAxis xAxis = mChart.getXAxis();
            xAxis.removeAllLimitLines();
            xAxis.enableGridDashedLine(10f, 10f, 0f);
            List<String> eventTypes = new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.event_types)));
            String[] eventNames = getResources().getStringArray(R.array.event_names);
            for (Map.Entry<Integer, String> event: events.entrySet()){
                int pos = eventTypes.indexOf(event.getValue());
                LimitLine eventLineChart = new LimitLine(event.getKey(), eventNames[pos]);
                eventLineChart.setLineWidth(4f);
                eventLineChart.enableDashedLine(10f, 10f, 0f);
                eventLineChart.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_TOP);
                eventLineChart.setTextSize(10f);
                xAxis.addLimitLine(eventLineChart);
            }
        } catch (Exception e) {
        }
        return new Pair<>(minValue, maxValue);
    }

    private LineDataSet getLineDataSet(ArrayList<Entry> data, int color, int circleColor, int fillColor, String label) {
        LineDataSet
                set1 = new LineDataSet(data, label);

        set1.setFillAlpha(110);
        set1.enableDashedLine(10f, 5f, 0f);
        set1.enableDashedHighlightLine(10f, 5f, 0f);
        set1.setColor(color);
        set1.setCircleColor(circleColor);
        set1.setLineWidth(1f);
        set1.setCircleRadius(1f);
        set1.setDrawCircleHole(false);
        set1.setValueTextSize(9f);
        set1.setDrawFilled(true);

        set1.setFillColor(fillColor);

        return set1;
    }

    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "START, x: " + me.getX() + ", y: " + me.getY());
    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
        Log.i("Gesture", "END, lastGesture: " + lastPerformedGesture);

        // un-highlight values after the gesture is finished and no single-tap
        if (lastPerformedGesture != ChartTouchListener.ChartGesture.SINGLE_TAP)
            mChart.highlightValues(null); // or highlightTouch(null) for callback to onNothingSelected(...)
    }

    @Override
    public void onChartLongPressed(MotionEvent me) {
        Log.i("LongPress", "Chart longpressed.");
    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {
        Log.i("DoubleTap", "Chart double-tapped.");
    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {
        Log.i("SingleTap", "Chart single-tapped.");
    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
        Log.i("Fling", "Chart flinged. VeloX: " + velocityX + ", VeloY: " + velocityY);
    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
        Log.i("Scale / Zoom", "ScaleX: " + scaleX + ", ScaleY: " + scaleY);
    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {
        Log.i("Translate / Move", "dX: " + dX + ", dY: " + dY);
    }

    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
        Log.i("Entry selected", e.toString());
        Log.i("LOWHIGH", "low: " + mChart.getLowestVisibleXIndex() + ", high: " + mChart.getHighestVisibleXIndex());
        Log.i("MIN MAX", "xmin: " + mChart.getXChartMin() + ", xmax: " + mChart.getXChartMax() + ", ymin: " + mChart.getYChartMin() + ", ymax: " + mChart.getYChartMax());
    }

    @Override
    public void onNothingSelected() {
        Log.i("Nothing selected", "Nothing selected.");
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.line, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.actionToggleValues: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setDrawValues(!set.isDrawValuesEnabled());
                }

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHighlight: {
                if(mChart.getData() != null) {
                    mChart.getData().setHighlightEnabled(!mChart.getData().isHighlightEnabled());
                    mChart.invalidate();
                }
                break;
            }
            case R.id.actionToggleFilled: {

                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawFilledEnabled())
                        set.setDrawFilled(false);
                    else
                        set.setDrawFilled(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCircles: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    if (set.isDrawCirclesEnabled())
                        set.setDrawCircles(false);
                    else
                        set.setDrawCircles(true);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.CUBIC_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.CUBIC_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleStepped: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.STEPPED
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.STEPPED);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionToggleHorizontalCubic: {
                List<ILineDataSet> sets = mChart.getData()
                        .getDataSets();

                for (ILineDataSet iSet : sets) {

                    LineDataSet set = (LineDataSet) iSet;
                    set.setMode(set.getMode() == LineDataSet.Mode.HORIZONTAL_BEZIER
                            ? LineDataSet.Mode.LINEAR
                            :  LineDataSet.Mode.HORIZONTAL_BEZIER);
                }
                mChart.invalidate();
                break;
            }
            case R.id.actionTogglePinch: {
                if (mChart.isPinchZoomEnabled())
                    mChart.setPinchZoom(false);
                else
                    mChart.setPinchZoom(true);

                mChart.invalidate();
                break;
            }
            case R.id.actionToggleAutoScaleMinMax: {
                mChart.setAutoScaleMinMaxEnabled(!mChart.isAutoScaleMinMaxEnabled());
                mChart.notifyDataSetChanged();
                break;
            }
            case R.id.animateX: {
                mChart.animateX(3000);
                break;
            }
            case R.id.animateY: {
                mChart.animateY(3000, Easing.EasingOption.EaseInCubic);
                break;
            }
            case R.id.animateXY: {
                mChart.animateXY(3000, 3000);
                break;
            }
            case R.id.actionSave: {
                if (mChart.saveToPath("title" + System.currentTimeMillis(), "")) {
                    Toast.makeText(getApplicationContext(), "Saving SUCCESSFUL!",
                            Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(getApplicationContext(), "Saving FAILED!", Toast.LENGTH_SHORT)
                            .show();

                // mChart.saveToGallery("title"+System.currentTimeMillis())
                break;
            }
        }
        return true;
    }*/

}
