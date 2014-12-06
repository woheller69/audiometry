package ut.ewh.audiometrytest;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestComplete extends ActionBarActivity {

    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
    SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_yyyy-HHmmss");
    String currentDateTime = sdf.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_complete);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
//        ActionBar actionbar = getSupportActionBar();
//        actionbar.setDisplayHomeAsUpEnabled(true);

        byte testResultsRightByte[] = new byte[7*8];

        try{
            FileInputStream fis = openFileInput("TestResults-Right-" + currentDateTime);
            fis.read(testResultsRightByte, 0, testResultsRightByte.length);
            fis.close();
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

        byte testResultsLeftByte[] = new byte[7 * 8];

        try{
            FileInputStream fis = openFileInput("TestResults-Left-" + currentDateTime);
            fis.read(testResultsLeftByte, 0, testResultsLeftByte.length);
            fis.close();
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
            row.setBackgroundColor(Color.parseColor("#424242"));
            TextView Values = new TextView(this);
            Values.setPadding(15, 0, 15, 0);
            Values.setGravity(Gravity.LEFT);
            Values.setTextSize(25.0f);
            Values.setTextColor(Color.parseColor("#FFFFFF"));
            Values.setText(testingFrequencies[i] + " Hz: " + String.format("%.2f", testResultsLeft[i]) + "db HL Left");
            row.addView(Values);
            tableResults.addView(row);

            TableRow row2 = new TableRow(this);
            row2.setLayoutParams(lp);
            row2.setPadding(15, 3, 15, 3);
            row2.setBackgroundColor(Color.parseColor("#424242"));
            TextView Values2 = new TextView(this);
            Values2.setPadding(15, 0, 15, 0);
            Values2.setGravity(Gravity.LEFT);
            Values2.setTextSize(25.0f);
            Values2.setTextColor(Color.parseColor("#FFFFFF"));
            Values2.setText(testingFrequencies[i] + " Hz: " + String.format("%.2f", testResultsRight[i]) + "db HL Right");
            row2.addView(Values);
            tableResults.addView(row2);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_complete, menu);
        return true;
    }

    public void gotoMain(View view){
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
