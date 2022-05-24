package org.woheller69.audiometry;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import static org.woheller69.audiometry.PerformTest.testFrequencies;
import static org.woheller69.audiometry.PerformTest.gain;

import androidx.appcompat.app.AppCompatActivity;


public class PerformSingleTest extends AppCompatActivity {
    private final int duration = 1;
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final int maxVolume = 32767;
    private final int minVolume = 0;
    private double actualVolume = -1;
    private Context context;
    double[] calibrationArray = new double[testFrequencies.length];
    private final Sound sound = new Sound();
    testThread testThread;
    TextView earView;
    TextView dBView;
    TextView frequencyView;
    Button volMinusView;
    Button volPlusView;
    Button freqPlusView;
    Button freqMinusView;
    private int s=0;
    private int i=0;

    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(PerformSingleTest.this, toast, Toast.LENGTH_SHORT).show());
    }

    public void setEarView(final int textID){
        runOnUiThread(() -> earView.setText(textID));
    }

    public void setFrequencyView(final int freq){
        runOnUiThread(() -> frequencyView.setText(freq + " Hz"));
    }

    public void setdBView(final double db){
        runOnUiThread(() -> dBView.setText(String.format("%.0f dB HL", db)));
    }

    /**
     * Randomly picks time gap between test tones in ms
     * @return
     */
    public int randomTime(){

        double num = Math.random();
        return (int) (1500+1500*num);
    }


    /**
     * go to MainActivity
     */
    public void gotoMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public class testThread extends Thread {

        private boolean stopped = false;

        public void stopThread(){
            stopped = true;
        }

        public void run() {
            AudioTrack audioTrack;
            FileOperations fileOperations = new FileOperations();
            calibrationArray=fileOperations.readCalibration(context);
            if (actualVolume==-1) actualVolume = (minVolume + maxVolume) / 2f;

            volPlusView.setOnClickListener(v -> {
                actualVolume = (actualVolume*Math.sqrt(2d));
                if (actualVolume>maxVolume) actualVolume=maxVolume;
            });
            volMinusView.setOnClickListener(v -> {
                actualVolume = (actualVolume/Math.sqrt(2d));
                if (actualVolume <= 1) {
                    showToast(getString(R.string.error_volume));
                    actualVolume = 1;
                }
            });
            earView.setOnClickListener(v -> s =(s + 1)%2);
            freqPlusView.setOnClickListener(v -> {
                int j=i;
                i=(i+1)%testFrequencies.length;
                actualVolume=actualVolume*Math.pow(10,(calibrationArray[i]-calibrationArray[j])/20);
                if (actualVolume>maxVolume) actualVolume=maxVolume;
            });
            freqMinusView.setOnClickListener(v -> {
                int j=i;
                i=(i+testFrequencies.length-1)%testFrequencies.length;
                actualVolume=actualVolume*Math.pow(10,(calibrationArray[i]-calibrationArray[j])/20);
                if (actualVolume>maxVolume) actualVolume=maxVolume;
            });
            while (!stopped) {
                int frequency = testFrequencies[i];
                if (s==0) setEarView(R.string.right_ear);
                else setEarView(R.string.left_ear);
                setFrequencyView(frequency);
                setdBView(20*Math.log10(actualVolume)-calibrationArray[i]);
                float increment = (float) (2*Math.PI) * frequency / sampleRate;
                audioTrack = sound.playSound(sound.genTone(increment,(int) actualVolume, numSamples), s, sampleRate);
                try {
                    Thread.sleep(randomTime());
                } catch (InterruptedException e) {}

                audioTrack.release();

            }
            if (stopped) return;

            gotoMain();
        }

    }


    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_performsingletest);
        earView = findViewById(R.id.ear);
        frequencyView = findViewById(R.id.frequency);
        dBView = findViewById(R.id.db);
        volMinusView = findViewById(R.id.vol_minus);
        volPlusView = findViewById(R.id.vol_plus);
        freqMinusView = findViewById(R.id.freq_minus);
        freqPlusView = findViewById(R.id.freq_plus);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));
    }

    @Override
    public void onResume() {
        gain=FileOperations.readGain(this);
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, gain,  0);
        testThread = new testThread();
        testThread.start();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_performsingle, menu);
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

    @Override
    public void onStop(){
        super.onStop();
        testThread.stopThread();
    }

}
