package org.woheller69.audiometry;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.GestureDetector;
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
    private GestureDetector gestureDetector;
    private boolean paused = false;
    private final int duration = 1;
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final int volume = 32767;
    static public final int lowGain = 4;
    static public final int highGain = 9;
    static public final int defaultGain = highGain;
    static public int gain = defaultGain;
    static public final int[] testFrequencies = {125, 250, 500, 1000, 2000, 3000, 4000, 6000, 8000};
    static final float[] correctiondBSPLtodBHL ={19.7f,9.0f,2.0f,0f,-3.7f,-8.1f,-7.8f, 2.1f,10.2f}; //estimated from  ISO226:2003 hearing threshold. Taken from https://github.com/IoSR-Surrey/MatlabToolbox/blob/master/%2Biosr/%2Bauditory/iso226.m Corrected to value=0 @1000Hz
    private boolean heard = false;
    private boolean skip = false;
    private boolean debug = false;
    public double[] thresholds_right = new double[testFrequencies.length];
    public double[] thresholds_left = new double[testFrequencies.length];
    private Context context;
    private final Sound sound = new Sound();
    testThread testThread;
    TextView earView;
    TextView frequencyView;
    TextView progressView;
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

            if (!intent.getStringExtra("Action").equals("Test")){  //if this was a full or simple calibration store calibration
                double[] calibrationArray = new double[testFrequencies.length+1];  //last field is used later for number of calibrations
                for(int i=0;i<testFrequencies.length;i++){  //for calibration average left/right channels
                    calibrationArray[i]=(thresholds_left[i]+thresholds_right[i])/2;
                }
                fileOperations.writeCalibration(calibrationArray, context);
            } else {  // store test result
                fileOperations.writeTestResult(thresholds_right, thresholds_left, context);
            }

            gotoMain();
        }

        public double singleTest(int s, int i) {
            AudioTrack audioTrack;
            int frequency = testFrequencies[i];
            setFrequencyView(frequency);
            float increment = (float) (2*Math.PI) * frequency / sampleRate;
            int actualVolume;
            int maxVolume = volume;
            int minVolume = 0;
            int thresVolume = maxVolume;
            // This is the loop for each individual sample using a binary search algorithm
            while (!stopped) {
                int tempResponse = 0;

                if (minVolume > 0){  //at least one tone not heard
                    actualVolume = (minVolume + maxVolume) / 2;
                } else {
                    actualVolume = (2 * minVolume + maxVolume) / 3;  // go down faster until tone not heard for first time
                }
                if (actualVolume <= 1) {
                    showToast(getString(R.string.error_volume));
                    actualVolume = 1;
                }
                if (debug) showToast(getString(R.string.debug_amplitude, actualVolume));

                if (minVolume > 0 && ((float) maxVolume / (float) minVolume) < Math.sqrt(2)) {  //if difference less than 3dB
                    return 20 * Math.log10(thresVolume);
                } else {
                    for (int z = 0; z < 3; z++) { //iterate three times per volume level
                        if (stopped) {
                            break;
                        }
                        if (paused) {
                            z = 0;
                            tempResponse = 0;
                        }
                        heard = false;
                        skip = false;
                        audioTrack = sound.playSound(sound.genTone(increment, actualVolume, numSamples), s, sampleRate);
                        try {
                            Thread.sleep(randomTime());
                        } catch (InterruptedException e) {
                        }
                        audioTrack.release();
                        if (heard) tempResponse++;
                        if (skip) tempResponse=3;
                         // Checks if the first two test were positive, and skips the third if true. Helps speed the test along.
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
                        if (minVolume > 0){ //at least one tone not heard
                            minVolume = actualVolume;
                        } else {
                            minVolume = (int) (actualVolume/Math.sqrt(2)); //if not heard for first time set minVolume 3dB below actualVolume. So we will test this level again if a higher level is heard
                        }

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
        this.gestureDetector = new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                heard = true;
                paused = false;
                PerformTest.this.runOnUiThread(bkgrndFlash);
                Timer timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        PerformTest.this.runOnUiThread(bkgrndFlashBlack);
                    }
                };
                timer.schedule(timerTask,250);
                progressView.setText(getString(R.string.test_running));
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                paused = !paused;
                progressView.setText(paused ? getString(R.string.test_paused) : getString(R.string.test_running));
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                skip = true;
                return true;
            }

        });
        setContentView(R.layout.activity_performtest);
        earView = findViewById(R.id.ear);
        frequencyView = findViewById(R.id.frequency);
        progressView = findViewById(R.id.progress);
        intent = getIntent();
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
    public boolean onTouchEvent(MotionEvent event) {
        this.gestureDetector.onTouchEvent(event);
        return true;
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
        if  (id == android.R.id.home ) {
            gotoMain();
        } else if ( id == R.id.debug) {
            debug = true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        super.onStop();
        testThread.stopThread();
    }

}
