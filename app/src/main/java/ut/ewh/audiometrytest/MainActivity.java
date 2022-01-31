package ut.ewh.audiometrytest;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends ActionBarActivity{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startTest = findViewById(R.id.main_startTest);
        Button testResults = findViewById(R.id.main_results);
        getSupportActionBar().getThemedContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO},1);
        if (FileOperations.isCalibrated(this)) {startTest.setVisibility(View.VISIBLE); testResults.setVisibility(View.VISIBLE);}
    }

    /**
     * goes to PreTestInformation activity
     * @param view- current view
     */
    public void gotoPreCalibration(View view){
        Intent intent = new Intent(this, Pre_Calibration.class);
        startActivity(intent);
    }

    /**
     * goes to TestProctoring activity
     * @param view- current view
     */
    public void gotoTest(View view){
        Intent intent = new Intent(this, PerformTest.class);
        intent.putExtra("Action","Test");
        startActivity(intent);
    }

    /**
     * goes to ExportData activity
     * @param view- current view
     */
    public void gotoExport(View view){
        Intent intent = new Intent(this, TestLookup.class);
        startActivity(intent);
    }
    public void gotoAcknowledgements(View view){
        Intent intent = new Intent(this, Acknowledgements.class);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
