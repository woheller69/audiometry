package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import static ut.ewh.audiometrytest.TestProctoring.testFrequencies;

public class TestData extends ActionBarActivity {

    double[][] testResults = new double[2][testFrequencies.length];
    String fileName;
    public final static String DESIRED_FILE = "ut.ewh.audiometrytest.DESIRED_FILE";
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        setContentView(R.layout.activity_test_data);
        Intent intent = getIntent();
        fileName = intent.getStringExtra(TestLookup.DESIRED_FILE);

        String[] names = fileName.split("-");

        String time = ""+ (names[2].charAt(0)) + (names[2].charAt(1)) + ":" + (names[2].charAt(2)) + (names[2].charAt(3));
        String name = "Test at " +time + ", " + names[1].replaceAll("_", ".");

        Button b = (Button) findViewById(R.id.share_button);
        b.setOnClickListener(view -> {
            String testdata = "Thresholds right\n";
            for (int i=0; i<testFrequencies.length;i++){
                testdata+=testFrequencies[i] + " " + testResults[0][i] + "\n";
            }
            testdata+="\nThresholds left\n";
            for (int i=0; i<testFrequencies.length;i++){
                testdata+=testFrequencies[i] + " " + testResults[0][i] + "\n";
            }
            testdata+="\n";
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(Intent.EXTRA_TEXT, testdata);
            startActivity(Intent.createChooser(sharingIntent, "Share in..."));
        });

        FileOperations fileOperations = new FileOperations();
        testResults=fileOperations.readTestData(fileName, context);

        Button d = (Button) findViewById(R.id.delete_button);
        d.setOnClickListener(view -> fileOperations.deleteTestData(fileName,context));

        TextView title = (TextView) findViewById(R.id.test_title);
        title.setText(name);

        // Draw Graph
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setNoDataTextDescription("Whoops! No data was found. Try again!");
        chart.setDescription("Hearing Thresholds (dB HL)");
        ArrayList<Entry> dataLeft = new ArrayList<Entry>();
        for (int i = 0; i < testResults[1].length; i ++){
            Entry dataPoint = new Entry((float) testResults[1][i] , i);
            dataLeft.add(dataPoint);
        }
        LineDataSet setLeft = new LineDataSet(dataLeft, "Left");
        setLeft.setCircleColor(getResources().getColor(R.color.green));
        setLeft.setColor(getResources().getColor(R.color.green));
        ArrayList<Entry> dataRight = new ArrayList<Entry>();
        for (int i = 0; i < testResults[0].length; i ++){
            Entry dataPoint = new Entry((float) testResults[0][i] , i);
            dataRight.add(dataPoint);
        }
        LineDataSet setRight = new LineDataSet(dataRight, "Right");
        setRight.setCircleColor(getResources().getColor(R.color.dark_orange));
        setRight.setColor(getResources().getColor(R.color.dark_orange));
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setLeft);
        dataSets.add(setRight);
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < testFrequencies.length; i++){
            xVals.add("" + testFrequencies[i]);
        }
        LineData data = new LineData(xVals, dataSets);

        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(Color.WHITE);
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setTextColor(Color.WHITE);
        Legend legend = chart.getLegend();
        legend.setTextColor(Color.WHITE);

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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
