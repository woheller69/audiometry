package org.woheller69.audiometry;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import java.io.File;
import java.util.Objects;

import static android.os.Environment.DIRECTORY_DOCUMENTS;


public class MainActivity extends AppCompatActivity {

    ActivityResultLauncher<Intent> mRestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().getThemedContext();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));
        checkShowInvisibleButtons();

        mRestore = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                      File intData = new File(Environment.getDataDirectory() + "//data//" + this.getPackageName());
                      if (result.getData()!=null && result.getData().getData()!=null) Backup.zipExtract(this, intData, result.getData().getData());
                      checkShowInvisibleButtons();
                });
    }

    private void checkShowInvisibleButtons(){
        Button startTest = findViewById(R.id.main_startTest);
        Button startSingleTest = findViewById(R.id.main_startSingleTest);
        Button testResults = findViewById(R.id.main_results);
        if (FileOperations.isCalibrated(this)) {
            startTest.setVisibility(View.VISIBLE);
            testResults.setVisibility(View.VISIBLE);
            startSingleTest.setVisibility(View.VISIBLE);
            if (GithubStar.shouldShowStarDialog(this)) GithubStar.starDialog(this,"https://github.com/woheller69/audiometer");
        }
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
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefManager.getInt("user",1)==1) {
            menu.findItem(R.id.user).setIcon(R.drawable.ic_user1_36dp);
        } else {
            menu.findItem(R.id.user).setIcon(R.drawable.ic_user2_36dp);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        File extStorage;
        File intData;
        int id = item.getItemId();
        if (id==R.id.backup) {
            intData = new File(Environment.getDataDirectory()+"//data//" + this.getPackageName() + "//files//");
            extStorage = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
            String filesBackup = getResources().getString(R.string.app_name)+".zip";
            final File zipFileBackup = new File(extStorage, filesBackup);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.main_backup));
            builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
                if (!Backup.checkPermissionStorage(this)) {
                    Backup.requestPermission(this);
                } else {
                    if (zipFileBackup.exists()){
                        if (!zipFileBackup.delete()){
                            Toast.makeText(this,getResources().getString(R.string.toast_delete), Toast.LENGTH_LONG).show();
                        }
                    }
                    try {
                        new ZipFile(zipFileBackup).addFolder(intData);
                    } catch (ZipException e) {
                        e.printStackTrace();
                    }
                }
            });
            builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        }else if (id==R.id.restore){
            intData = new File(Environment.getDataDirectory() + "//data//" + this.getPackageName());
            extStorage = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOCUMENTS);
            String filesBackup = getResources().getString(R.string.app_name)+".zip";
            final File zipFileBackup = new File(extStorage, filesBackup);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getResources().getString(R.string.main_restore_message));
            builder.setPositiveButton(R.string.dialog_OK_button, (dialog, whichButton) -> {
                if (!Backup.checkPermissionStorage(this)) {
                    Backup.requestPermission(this);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.setType("application/zip");
                        mRestore.launch(intent);
                    } else {
                        Backup.zipExtract(this, intData, Uri.fromFile(zipFileBackup));
                        checkShowInvisibleButtons();
                    }
                }
            });
            builder.setNegativeButton(R.string.dialog_NO_button, (dialog, whichButton) -> dialog.cancel());
            AlertDialog dialog = builder.create();
            dialog.show();
            Objects.requireNonNull(dialog.getWindow()).setGravity(Gravity.BOTTOM);
        } else if (id==R.id.user){
            SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(this);
            if (prefManager.getInt("user",1)==1){
                item.setIcon(R.drawable.ic_user2_36dp);
                SharedPreferences.Editor editor = prefManager.edit();
                editor.putInt("user", 2);
                editor.apply();
            } else {
                item.setIcon(R.drawable.ic_user1_36dp);
                SharedPreferences.Editor editor = prefManager.edit();
                editor.putInt("user", 1);
                editor.apply();
            }
            invalidateOptionsMenu();
        }

        return super.onOptionsItemSelected(item);
    }
}
