package ut.ewh.audiometrytest;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class Pre_Calibration extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre__calibration);
        Button skip = findViewById(R.id.skip);
        Button delete = findViewById(R.id.delete);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        if (FileOperations.isCalibrated(this)) {skip.setVisibility(View.VISIBLE);delete.setVisibility(View.VISIBLE);}
    }

    public void gotoCalibration(View view){
        Intent intent = new Intent(this, TestProctoring.class);
        intent.putExtra("Action","Calibrate");
        startActivity(intent);
    }

    public void deleteCalibration(View view){
        FileOperations fileOperations = new FileOperations();
        fileOperations.deleteCalibration(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void startTest(View view){
        Intent intent = new Intent(this, TestProctoring.class);
        intent.putExtra("Action","Test");
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pre__calibration, menu);
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
