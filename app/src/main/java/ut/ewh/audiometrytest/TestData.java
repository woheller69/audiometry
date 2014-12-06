package ut.ewh.audiometrytest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class TestData extends ActionBarActivity {

    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
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

        Button b = (Button) findViewById(R.id.email_button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoExport(view, fileName);
            }
        });

        TextView title = (TextView) findViewById(R.id.test_title);
        title.setText("Test Results for " + fileName);

        byte testResultsRightByte[] = new byte[7*8];

        try{
            FileInputStream fis = openFileInput(fileName);
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

        TableLayout tableResults = (TableLayout) findViewById(R.id.tableResults);
        tableResults.setPadding(15, 3, 15, 3);


        for (int i = 0; i < 7; i ++) {
            TableRow row = new TableRow(this);
            TableLayout.LayoutParams lp = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
            row.setLayoutParams(lp);
            row.setPadding(20, 3, 15, 3);
            row.setBackgroundColor(Color.parseColor("#424242"));
            TextView Values = new TextView(this);
            Values.setPadding(20, 0, 15, 0);
            Values.setGravity(Gravity.LEFT);
            Values.setTextSize(25.0f);
            Values.setTextColor(Color.parseColor("#FFFFFF"));
            Values.setText(testingFrequencies[i] + " Hz: " + String.valueOf(testResultsRight[i]));
            row.addView(Values);
            tableResults.addView(row);
        }

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
