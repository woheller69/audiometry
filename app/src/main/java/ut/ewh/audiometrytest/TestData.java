package ut.ewh.audiometrytest;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class TestData extends ActionBarActivity {

    private final int[] frequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
    public final static String DESIRED_FILE = "ut.ewh.audiometrytest.DESIRED_FILE";

    public void gotoExport(View view, String string) {
        Intent exportIntent = new Intent(this, ExportData.class);
        exportIntent.putExtra(DESIRED_FILE, string);
        startActivity(exportIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

        setContentView(R.layout.activity_test_data);
        Intent intent = getIntent();
        final String fileName = intent.getStringExtra(TestLookup.DESIRED_FILE);

        String[] names = fileName.split("-");
        String fileNameLeft = names[0] + "-Left-" + names[2] + "-" + names[3];
        String time = "";
        for (int j=0;j<4;j = j + 2){
            if (j != 2){
                time += String.valueOf(names[3].charAt(j)) + String.valueOf(names[3].charAt(j+1)) + ":";
            } else {
                time += String.valueOf(names[3].charAt(j)) + String.valueOf(names[3].charAt(j+1));
            }
        }
        String name = "Test at " +time + ", " + names[2].replaceAll("_", ".");

        Button b = (Button) findViewById(R.id.email_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExport(view, fileName);
            }
        });

        TextView title = (TextView) findViewById(R.id.test_title);
        title.setText(name);

        byte[] testResultsRightByte = new byte[7*8];

        try{
            FileInputStream fis = openFileInput(fileName);
            fis.read(testResultsRightByte, 0, testResultsRightByte.length);
            fis.close();
        } catch (IOException e) {};

        byte[] testResultsLeftByte = new byte[7*8];

        try{
            FileInputStream fis = openFileInput(fileNameLeft);
            fis.read(testResultsLeftByte, 0, testResultsLeftByte.length);
            fis.close();
        } catch (IOException e) {};


        final double[] testResultsRight = new double[7];
        final double[] testResultsLeft = new double[7];

        int counter = 0;

        for (int i = 0; i < testResultsRight.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsRightByte[counter];
                counter++;
            }
            testResultsRight[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }

        counter = 0;

        for (int i = 0; i < testResultsLeft.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsLeftByte[counter];
                counter++;
            }
            testResultsLeft[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }

        // Draw Graph
        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setNoDataTextDescription("Whoops! No data was found. Try again!");
        chart.setDescription("Hearing Thresholds (dB HL)");
        ArrayList<Entry> dataLeft = new ArrayList<Entry>();
        for (int i = 0; i < testResultsLeft.length; i ++){
            Entry dataPoint = new Entry((float) testResultsLeft[i] , i);
            dataLeft.add(dataPoint);
        }
        LineDataSet setLeft = new LineDataSet(dataLeft, "Left");
        setLeft.setCircleColor(getResources().getColor(R.color.green));
        setLeft.setColor(getResources().getColor(R.color.green));
        ArrayList<Entry> dataRight = new ArrayList<Entry>();
        for (int i = 0; i < testResultsRight.length; i ++){
            Entry dataPoint = new Entry((float) testResultsRight[i] , i);
            dataRight.add(dataPoint);
        }
        LineDataSet setRight = new LineDataSet(dataRight, "Right");
        setRight.setCircleColor(getResources().getColor(R.color.dark_orange));
        setRight.setColor(getResources().getColor(R.color.dark_orange));
        ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
        dataSets.add(setLeft);
        dataSets.add(setRight);
        ArrayList<String> xVals = new ArrayList<String>();
        for (int i = 0; i < frequencies.length; i++){
            xVals.add("" + frequencies[i]);
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
