package ut.ewh.audiometrytest;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class TestComplete extends ActionBarActivity {

    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        byte testResultsRightByte[] = new byte[7*8];

        try{
            FileInputStream fis = openFileInput("TestResultsRight");
            fis.read(testResultsRightByte, 0, testResultsRightByte.length);
            fis.close();
            //Log.i("File Read Info", "File Read Successful");
        } catch (IOException e) {};

        final double testResultsRight[] = new double[7];


        int counter = 0;

        for (int i = 0; i < testResultsRight.length; i++){
            byte tmpByteBuffer[] = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsRightByte[counter];
                counter++;
            }
            testResultsRight[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }
        //Log.i("Calibration Data", "Calibration factors are: " + testResultsRight[0] + " " + testResultsRight[1] + " " + testResultsRight[2] + " " + testResultsRight[3] + " " + testResultsRight[4] + " " + testResultsRight[5] + " " + testResultsRight[6]);

        byte testResultsLeftByte[] = new byte[7 * 8];

        try{
            FileInputStream fis = openFileInput("TestResultsLeft");
            fis.read(testResultsLeftByte, 0, testResultsLeftByte.length);
            fis.close();
            //Log.i("File Read Info", "File Read Successful");
        } catch (IOException e) {};


        final double testResultsLeft[] = new double[7];


        counter = 0;

        for (int i = 0; i < testResultsLeft.length; i++){
            byte tmpByteBuffer[] = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsLeftByte[counter];
                counter++;
            }
            testResultsLeft[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }

        TableLayout tableResults = (TableLayout) findViewById(R.id.tableResults);
        tableResults.setPadding(15, 3, 15, 3);


        for (int i = 0; i < 7; i ++) {
            TableRow row = new TableRow(this);
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.setPadding(15, 3, 15, 3);
            row.setBackgroundColor(Color.parseColor("#FFFFFF"));

            TextView Values = new TextView(this);
            Values.setPadding(15, 0, 15, 0);
            Values.setGravity(Gravity.LEFT);
            Values.setTextSize(15.0f);
            Values.setTextColor(Color.parseColor("#000000"));
            Values.setText(testingFrequencies[i] + " Hz Right: " + String.valueOf(testResultsRight[i]));
            row.addView(Values);
            tableResults.addView(row);
        }
        for (int i = 0; i < 7; i ++) {
            TableRow row = new TableRow(this);
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.setPadding(15, 3, 15, 3);
            row.setBackgroundColor(Color.parseColor("#FFFFFF"));
            TextView Values = new TextView(this);
            Values.setPadding(15, 0, 15, 0);
            Values.setGravity(Gravity.LEFT);
            Values.setTextSize(15.0f);
            Values.setTextColor(Color.parseColor("#000000"));
            Values.setText(testingFrequencies[i] + " Hz Left: " + String.valueOf(testResultsLeft[i]));
            row.addView(Values);
            tableResults.addView(row);
        }



//        ProgressBar progressBar = new ProgressBar(this);
//        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
//        progressBar.setIndeterminate(true);
//
//        getListView().setEmptyView(progressBar);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_complete, menu);
        return true;
    }

    public void gotoExport(View view){
        Intent intent = new Intent(this, ExportData.class);
        startActivity(intent);
    }

    public void tmpGotoMain(View view){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
