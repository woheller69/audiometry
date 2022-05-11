package org.woheller69.audiometry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import static org.woheller69.audiometry.PerformTest.testFrequencies;

public class TestData extends AppCompatActivity {
    int index;
    String[] allSavedTests;
    double[][] testResults = new double[2][testFrequencies.length];
    double[] calibrationArray = new double[testFrequencies.length];
    String fileName;
    private final float YMIN = -20f;
    private final float YMAX = 100f;
    private Context context;
    private LineChart chart;
    private boolean zoomed = false;

    public float scaleCbr(double cbr) {
        return (float) (Math.log10(cbr/125)/Math.log10(2));
    }

    public float unScaleCbr(double cbr) {
        double calcVal = Math.pow(2,cbr)*125;
        return (float)(calcVal);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        allSavedTests=TestLookup.getAllSavedTests(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));

        setContentView(R.layout.activity_test_data);
        Intent intent = getIntent();
        index = intent.getIntExtra("Index",0);
        fileName = allSavedTests[index];

        ImageButton next = (ImageButton) findViewById(R.id.next);
        next.setOnClickListener(view -> {
            index = (index - 1);
            if (index < 0) index = 0;
            fileName = allSavedTests[index];
            zoomed = false;
            draw();
        });

        ImageButton prev = (ImageButton) findViewById(R.id.prev);
        prev.setOnClickListener(view -> {
            index = (index + 1);
            if (index > allSavedTests.length-1) index = allSavedTests.length-1;
            fileName = allSavedTests[index];
            zoomed = false;
            draw();
        });

        draw();

    }

    private void draw() {
        String[] names = fileName.split("-");
        String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Long.parseLong(names[1])) + ", " + DateFormat.getDateInstance(DateFormat.SHORT).format(Long.parseLong(names[1]));

        ImageButton share = (ImageButton) findViewById(R.id.share_button);
        share.setOnClickListener(view -> {
            String testdata = "Thresholds right\n";
            for (int i=0; i<testFrequencies.length;i++){
                testdata+=testFrequencies[i] + " Hz " + String.format("%.1f",(float) (testResults[0][i]-calibrationArray[i])) + " dBHL\n";
            }
            testdata+="\nThresholds left\n";
            for (int i=0; i<testFrequencies.length;i++){
                testdata+=testFrequencies[i] + " Hz " + String.format("%.1f",(float) (testResults[1][i]-calibrationArray[i])) + " dBHL\n";
            }
            testdata+="\n";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, testdata);
            startActivity(Intent.createChooser(sharingIntent, "Share in..."));
        });

        ImageButton zoom = findViewById(R.id.zoom_button);
        zoom.setImageDrawable(zoomed ? ContextCompat.getDrawable(this,R.drawable.ic_zoom_out_black_24dp) : ContextCompat.getDrawable(this,R.drawable.ic_zoom_in_black_24dp));
        zoom.setOnClickListener(view -> {
            if (!zoomed){
                chart.getAxisLeft().resetAxisMaximum();
                chart.getAxisLeft().resetAxisMinimum();
                chart.getAxisRight().resetAxisMaximum();
                chart.getAxisRight().resetAxisMinimum();
                zoom.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_zoom_out_black_24dp));
                zoomed = true;
            } else {
                chart.getAxisLeft().setAxisMinimum(YMIN);
                chart.getAxisLeft().setAxisMaximum(YMAX);
                chart.getAxisRight().setAxisMinimum(YMIN);
                chart.getAxisRight().setAxisMaximum(YMAX);
                zoom.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.ic_zoom_in_black_24dp));
                zoomed = false;
            }
            chart.notifyDataSetChanged();
            chart.invalidate();
        });

        FileOperations fileOperations = new FileOperations();
        testResults=fileOperations.readTestData(fileName, context);
        calibrationArray=fileOperations.readCalibration(context);

        ImageButton delete = (ImageButton) findViewById(R.id.delete_button);
        delete.setOnClickListener(view -> {
            fileOperations.deleteTestData(fileName,context);
            allSavedTests=TestLookup.getAllSavedTests(this);
            if (index > allSavedTests.length-1) index = allSavedTests.length-1;
            fileName = allSavedTests[index];
            zoomed = false;
            draw();});

        TextView title = (TextView) findViewById(R.id.test_title);
        title.setText(time);

        // Draw Graph
        chart = (LineChart) findViewById(R.id.chart);
        chart.setExtraTopOffset(10);
        chart.setNoDataText("Whoops! No data was found. Try again!");
        Description description = new Description();
        description.setText(getResources().getString(R.string.chart_description));
        description.setTextSize(15);
        description.setTextColor(getResources().getColor(R.color.white,getTheme()));
        chart.setDescription(description);

        ArrayList<Entry> dataLeft = new ArrayList<Entry>();
        for (int i = 0; i < testResults[1].length; i ++){
            Entry dataPoint = new Entry( scaleCbr(testFrequencies[i]),(float) (testResults[1][i]-calibrationArray[i]) );
            dataLeft.add(dataPoint);
        }
        LineDataSet setLeft = new LineDataSet(dataLeft, getString(R.string.left));
        setLeft.setCircleColor(getResources().getColor(R.color.green,getTheme()));
        setLeft.setColor(getResources().getColor(R.color.green,getTheme()));
        setLeft.setValueTextColor(Color.WHITE);
        setLeft.setValueTextSize(12);

        ArrayList<Entry> dataRight = new ArrayList<Entry>();
        for (int i = 0; i < testResults[0].length; i ++){
            Entry dataPoint = new Entry( scaleCbr(testFrequencies[i]), (float)(testResults[0][i]-calibrationArray[i]));
            dataRight.add(dataPoint);
        }
        LineDataSet setRight = new LineDataSet(dataRight, getString(R.string.right));
        setRight.setCircleColor(getResources().getColor(R.color.primary_dark,getTheme()));
        setRight.setColor(getResources().getColor(R.color.primary_dark,getTheme()));
        setRight.setValueTextColor(Color.WHITE);
        setRight.setValueTextSize(12);

        LineData data = new LineData(setLeft,setRight);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(15);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(YMIN);
        leftAxis.setAxisMaximum(YMAX);
        leftAxis.setTextSize(15);
        leftAxis.setInverted(true);
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setAxisMinimum(YMIN);
        rightAxis.setAxisMaximum(YMAX);
        rightAxis.setInverted(true);
        rightAxis.setTextSize(15);
        rightAxis.setTextColor(Color.WHITE);
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);
        legend.setTextSize(15);

        xAxis.setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                DecimalFormat mFormat;
                mFormat = new DecimalFormat("##0.#"); // use one decimal.
                    return mFormat.format(unScaleCbr(value));
            }
            @Override
            public int getDecimalDigits() {
                return 0;
            }
        });

        chart.setData(data);
        chart.invalidate(); // refresh
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id){
            case android.R.id.home:
                gotoExport();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void gotoExport(){
        Intent intent = new Intent(this, TestLookup.class);
        startActivity(intent);
    }
}
