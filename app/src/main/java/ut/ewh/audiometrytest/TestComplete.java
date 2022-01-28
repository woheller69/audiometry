package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import static ut.ewh.audiometrytest.TestProctoring.testFrequencies;

public class TestComplete extends ActionBarActivity {
    double[][] testResults = new double[2][testFrequencies.length];
    Context context;
    SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_yyyy-HHmmss");
    String currentDateTime = sdf.format(new Date());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_test_complete);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
//        ActionBar actionbar = getSupportActionBar();
//        actionbar.setDisplayHomeAsUpEnabled(true);

        FileOperations fileOperations = new FileOperations();
        testResults=fileOperations.readTestData("TestResults-Right-" + currentDateTime, "TestResults-Left-" + currentDateTime, context);

        TableLayout tableResults = (TableLayout) findViewById(R.id.tableResults);
        tableResults.setPadding(15, 3, 15, 3);


        for (int i = 0; i < testFrequencies.length; i ++) {
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
            Values.setText(testFrequencies[i] + " Hz: " + String.format("%.2f", testResults[0][i]) + "db HL Left");
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
            Values2.setText(testFrequencies[i] + " Hz: " + String.format("%.2f", testResults[1][i]) + "db HL Right");
            row2.addView(Values2);
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
