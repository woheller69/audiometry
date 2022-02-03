package org.woheller69.audiometry;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;


public class Pre_Calibration extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre__calibration);
        Button delete = findViewById(R.id.delete);
        TextView num = findViewById(R.id.numCalibrations);
        FileOperations fileOperations = new FileOperations();
        num.setText(String.format(getResources().getString(R.string.num_calibrations), fileOperations.readNumCalibrations(this)));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));
        if (FileOperations.isCalibrated(this)) {delete.setVisibility(View.VISIBLE);}




    }

    public void gotoCalibration(View view){
        Intent intent = new Intent(this, PerformTest.class);
        intent.putExtra("Action","FullCalibration");
        startActivity(intent);
    }

    public void gotoSimpleCalibration(View view){
        Intent intent = new Intent(this, PerformTest.class);
        intent.putExtra("Action","SimpleCalibration");
        startActivity(intent);
    }

    public void deleteCalibration(View view){
        FileOperations fileOperations = new FileOperations();
        fileOperations.deleteCalibration(this);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void gotoMain(){
        Intent intent = new Intent(this, MainActivity.class);
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
        switch (id){
            case android.R.id.home:
                gotoMain();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
