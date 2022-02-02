package org.woheller69.audiometry;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class PerformTest extends AppCompatActivity {
    private final int duration = 1;
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final int volume = 32767;
    static public final int[] testFrequencies = {125, 250, 500, 1000, 2000, 3000, 4000, 6000, 8000};
    static final float[] correctiondBSPLtodBHL ={19.7f,9.0f,2.0f,0f,-3.7f,-8.1f,-7.8f, 2.1f,10.2f}; //estimated from  ISO226:2003 hearing threshold. Taken from https://github.com/IoSR-Surrey/MatlabToolbox/blob/master/%2Biosr/%2Bauditory/iso226.m Corrected to value=0 @1000Hz
    private boolean heard = false;
    public double[] thresholds_right = new double[testFrequencies.length];
    public double[] thresholds_left = new double[testFrequencies.length];
    private Context context;
    private final Sound sound = new Sound();
    testThread testThread;
    TextView earView;
    TextView frequencyView;
    Intent intent;



    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(PerformTest.this, toast, Toast.LENGTH_SHORT).show());
    }

    public void setEarView(final int textID){
        runOnUiThread(() -> earView.setText(textID));
    }

    public void setFrequencyView(final int freq){
        runOnUiThread(() -> frequencyView.setText(freq + " Hz"));
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
     * Changes background to white when called.
     */
    Runnable bkgrndFlash = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundColor(getResources().getColor(R.color.green,getTheme()));
        }
    };
    /**
     * Changes background color to black when called
     */
    Runnable bkgrndFlashBlack = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundColor(getResources().getColor(R.color.background_grey,getTheme()));
        }
    };

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

            //iterated once for every frequency to be tested
            for (int s = 0; s < 2; s++) {
                if (s==0) setEarView(R.string.right_ear);
                else setEarView(R.string.left_ear);
                if (stopped){break;}
                if (!intent.getStringExtra("Action").equals("SimpleCalibration")) {
                    for (int i = 0; i < testFrequencies.length; i++) {
                        double threshold = singleTest(s, i);
                        if (s == 0) {
                            thresholds_right[i] = threshold; //records volume as threshold
                        } else {
                            thresholds_left[i] = threshold; //records volume as threshold
                        }
                    }
                }else{
                    double threshold = singleTest(s, Arrays.binarySearch(testFrequencies, 1000));  // Test at 1000Hz
                    if (s == 0) {
                        for (int i=0;i<testFrequencies.length;i++) thresholds_right[i] = correctiondBSPLtodBHL[i] + threshold;
                    } else {
                        for (int i=0;i<testFrequencies.length;i++) thresholds_left[i] = correctiondBSPLtodBHL[i] + threshold;
                    }
                }

                PerformTest.this.runOnUiThread(bkgrndFlashBlack);
            }
            if (stopped) return;


            FileOperations fileOperations = new FileOperations();
            if (!intent.getStringExtra("Action").equals("SimpleCalibration")){
                fileOperations.writeTestResult(thresholds_right, thresholds_left, context);
            }

            if (!intent.getStringExtra("Action").equals("Test")){
                double[] calibrationArray = new double[testFrequencies.length+1];  //last field is used later for number of calibrations
                for(int i=0;i<testFrequencies.length;i++){  //for calibration average left/right channels
                    calibrationArray[i]=(thresholds_left[i]+thresholds_right[i])/2;
                }
                fileOperations.writeCalibration(calibrationArray, context);
            }

            gotoMain();
        }

        public double singleTest(int s, int i) {
            AudioTrack audioTrack;
            int frequency = testFrequencies[i];
            setFrequencyView(frequency);
            float increment = (float) (2*Math.PI) * frequency / sampleRate;
            int maxVolume = volume;
            int minVolume = 0;
            int thresVolume = maxVolume;
            // This is the loop for each individual sample using a binary search algorithm
            while (!stopped) {
                int tempResponse = 0;
                int actualVolume = (minVolume + maxVolume) / 2;

                if (minVolume > 0 && ((float) maxVolume / (float) minVolume) < Math.sqrt(2)) {
                    return 20 * Math.log10(thresVolume);
                } else {
                    for (int z = 0; z < 3; z++) { //iterate three times per volume level
                        if (stopped) {
                            break;
                        }
                        heard = false;
                        audioTrack = sound.playSound(sound.genTone(increment, actualVolume, numSamples), s, sampleRate);
                        try {
                            Thread.sleep(randomTime());
                        } catch (InterruptedException e) {
                        }
                        audioTrack.release();
                        if (heard) {
                            tempResponse++;
                        }
//                                // Checks if the first two test were positive, and skips the third if true. Helps speed the test along.
                        if (tempResponse >= 2) {
                            break;
                        }
                        // Check if the first two tests were misses, and skips the third if this is the case.
                        if (z == 1 && tempResponse == 0) {
                            break;
                        }
                    }
                    //If the response was positive two out of three times, register as heard
                    if (tempResponse >= 2) {
                        thresVolume = actualVolume;
                        maxVolume = actualVolume;
                    } else {
                        minVolume = actualVolume;
                    }
                } //continue with test
            }
            return 0;
        }


    }


    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=this;
        setContentView(R.layout.activity_performtest);
        earView = findViewById(R.id.ear);
        frequencyView = findViewById(R.id.frequency);
        intent = getIntent();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 9,  0);
        testThread = new testThread();
        testThread.start();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e){
        heard = true;
        PerformTest.this.runOnUiThread(bkgrndFlash);
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                PerformTest.this.runOnUiThread(bkgrndFlashBlack);
            }
        };
        timer.schedule(timerTask,250);
        return super.dispatchTouchEvent(e);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_perform, menu);
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
