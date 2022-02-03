package org.woheller69.audiometry;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


public class MainActivity extends AppCompatActivity {
    public File sd;
    public File data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button startTest = findViewById(R.id.main_startTest);
        Button startSingleTest = findViewById(R.id.main_startSingleTest);
        Button testResults = findViewById(R.id.main_results);
        getSupportActionBar().getThemedContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));
        requestPermissions( new String[]{Manifest.permission.RECORD_AUDIO},1);
        if (FileOperations.isCalibrated(this)) {
            startTest.setVisibility(View.VISIBLE);
            testResults.setVisibility(View.VISIBLE);
            startSingleTest.setVisibility(View.VISIBLE);
            if (GithubStar.shouldShowStarDialog(this)) GithubStar.starDialog(this,"https://github.com/woheller69/audiometer");
        }

        sd = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
        data = Environment.getDataDirectory();
        String files = "//data//" + this.getPackageName();
        String files_backup = "hEARtest";
        final File previewsFolder_app = new File(data, files);
        final File previewsFolder_backup = new File(sd, files_backup);

        Button ib_backup = findViewById(R.id.backup);
        ib_backup.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Backup Data");
            builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
                if (!Backup.checkPermissionStorage(this)) {
                    Backup.requestPermission(this);
                } else {
                    Backup.copyDirectory(previewsFolder_app, previewsFolder_backup);
                }
            });
            builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        });

        Button ib_restore = findViewById(R.id.restore);
        ib_restore.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Restore Data");
            builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
                if (!Backup.checkPermissionStorage(this)) {
                    Backup.requestPermission(this);
                } else {
                    Backup.copyDirectory(previewsFolder_backup, previewsFolder_app);
                }
            });
            builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        });

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
     * goes to PerformTest activity
     * @param view- current view
     */
    public void gotoTest(View view){
        Intent intent = new Intent(this, PerformTest.class);
        intent.putExtra("Action","Test");
        startActivity(intent);
    }

    /**
     * goes to PerformTest activity
     * @param view- current view
     */
    public void gotoSingleTest(View view){
        Intent intent = new Intent(this, PerformSingleTest.class);
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
    public void gotoInfo(View view){
        Intent intent = new Intent(this, Info.class);
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

        return super.onOptionsItemSelected(item);
    }

}
