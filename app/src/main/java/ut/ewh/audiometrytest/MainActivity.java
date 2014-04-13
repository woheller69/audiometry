package ut.ewh.audiometrytest;

import android.content.Context;
import android.media.AudioManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.util.Log;


public class MainActivity extends ActionBarActivity {


    //----------------------------------------------------------------
    //Here Ends The Methods and Variables- below be dragons
    //----------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

        for (int j = 0; j <=15; j++) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, j, AudioManager.FLAG_PLAY_SOUND);
            try{
                Thread.sleep(400);
            } catch(InterruptedException e){};
        }*/
    }

    public void gotoPretest(View view){
        Intent intent = new Intent(this, PreTestInformation.class);
        startActivity(intent);
    }
    public void gotoExport(View view){
        Intent intent = new Intent(this, ExportData.class);
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
