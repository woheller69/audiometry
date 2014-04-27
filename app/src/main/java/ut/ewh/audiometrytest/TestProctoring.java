package ut.ewh.audiometrytest;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;
import java.util.Random;



public class TestProctoring extends ActionBarActivity {
    private final int duration = 1;
    private final int sampleRate = 22050;
    private final int numSamples = duration * sampleRate;
    private final int volume = 32767;
    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
    private final int right = 42;
    private final int left = 24;


    private boolean heard = false;
    private boolean loop = true;
    int a = 0;

    public int[] thresholds_right = {0, 0, 0, 0, 0, 0, 0};
    public int[] thresholds_left = {0, 0, 0, 0, 0, 0, 0};


    /**
     * Randomly picks time gap between test tones in ms
     * @return
     */
    public int randomTime(){
        int time = 3000;
        double num = Math.random();
        if (num < 0.3){
            time = 2000;
        } else if (num < 0.67 && num >= 0.3){
            time = 3000;
        }else{
            time = 4000;
        };
        return time;
    }

    /**
     * Changes background to white when called.
     */
    Runnable bkgrndFlash = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundResource(R.color.green);
            //bkgrnd.postDelayed(this, 1000);
        }
    };
    /**
     * Changes background color to black when called
     */
    Runnable bkgrndFlashBlack = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundResource(R.color.background_grey);
            //bkgrnd.postDelayed(this, 1000);
        }
    };

    /**
     * go to TestComplete activity
     */
    public void gotoComplete(){
        Intent intent = new Intent(this, TestComplete.class);
        startActivity(intent);
    };

    /**
     * Generates the tone based on the increment and volume, used in inner loop
     * @param increment - the amount to increment by
     * @param volume - the volume to generate
     */
    public byte[] genTone(float increment, int volume){
        float angle = 0;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];
        for (int i = 0; i < numSamples; i++){
            sample[i] = Math.sin(angle);
            angle += increment;
        }
        int idx = 0;
        for (final double dVal : sample) {
            final short val = (short) ((dVal * volume));
            //volume controlled by the value multiplied by dVal; max value is 32767
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSnd;
    }

    /**
     * Writes the parameter byte array to an AudioTrack and plays the array
     * @param generatedSnd- input 16-bit PCM Array
     */
    public void playSound(byte[] generatedSnd, int ear) {
        //final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        // audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        //audioTrack.flush();
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        if (ear == 42) {
            audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        } else {
            audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), 0);
        }
        audioTrack.play();
       // audioTrack.release();
    }

    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_proctoring);
        ActionBar actionbar = getSupportActionBar();

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 9,  0);
        //Executes playback and threshold searching procedure
        Thread testThread = new Thread (new Runnable() {
            public void run() {
                //iterated once for every frequency to be tested
                int ear;
                for (int s = 0; s < 2; s++) {
                    if (s == 0) {
                        ear = 42;
                    } else {
                        ear = 24;
                    }
                    for (int i = 0; i < testingFrequencies.length; i++) {
                        int frequency = testingFrequencies[i];
                        float increment = (float) (Math.PI) * frequency / sampleRate;
                        int maxVolume = volume;
                        int minVolume = 0;
                        // This is the loop for each individual sample using a binary search algorithm
                        for (; ; ) {
                            int tempResponse = 0;
                            int actualVolume = (minVolume + maxVolume) / 2;
                            if ((maxVolume - minVolume) < 200) { //the test is done if the range is less than 400
                                if (s == 0) {
                                    thresholds_right[i] = actualVolume; //records volume as threshold
                                    Log.i("Temporary Results", "results are " + thresholds_right[0] + " " + thresholds_right[1] + " " + thresholds_right[2] + " " + thresholds_right[3] + " " + thresholds_right[4] + " " + thresholds_right[5] + " " + thresholds_right[6]);
                                } else {
                                    thresholds_left[i] = actualVolume; //records volume as threshold
                                    Log.i("Temporary Results", "results are " + thresholds_left[0] + " " + thresholds_left[1] + " " + thresholds_left[2] + " " + thresholds_left[3] + " " + thresholds_left[4] + " " + thresholds_left[5] + " " + thresholds_left[6]);

                                }
                                break; //go to next frequency
                            } else {
                                for (int z = 0; z < 3; z++) { //iterate three times per volume level
                                    heard = false;
                                    Log.i("Playback Info", "actual volume is" + actualVolume);
                                    playSound(genTone(increment, actualVolume), ear);
                                    try {
                                        Thread.sleep(randomTime());
                                    } catch (InterruptedException e) {
                                    }
                                    ;
                                    if (heard) {
                                        tempResponse++;
                                    }
                                }
                                //If the response was positive two out of three times, register as heard
                                if (tempResponse >= 2) {
                                    maxVolume = actualVolume;
                                } else {
                                    minVolume = actualVolume;
                                }
                            } //continue with test
                        }
                        ;
                        Log.i("Loop Alert", "New Frequency Beginning " + heard);

                    }
                    //Log.i("Final Results", "results are " + thresholds[0] + " " + thresholds[1] + " " + thresholds[2] + " " + thresholds[3] + " " + thresholds[4] + " " + thresholds[5] + " " + thresholds[6]);
                    loop = false;
                    TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                }
                gotoComplete();

            }
        });
        Thread timingThread = new Thread (new Runnable() {
            public void run() {
                while (loop) {
                    if (heard) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException x) {
                        }
                        ;
                        TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                    }
                }
            }
        });

        Thread screenThread = new Thread (new Runnable() {
            public void run(){
                while (loop){
                    if (heard){
                        TestProctoring.this.runOnUiThread(bkgrndFlash);
						/*try {
							Thread.sleep(1000);
						} catch (InterruptedException x){};*/
                        while (heard){

                        };
                        //TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                    };
                };
            };
        });
        testThread.start();
        screenThread.start();
        timingThread.start();
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent e){
        //View view = findViewById(R.id.page);
        //view.setBackgroundResource(R.color.white);
        Log.i("Touch Alert", "Screen was hit!" + a++ + heard);
        heard = true;
        //bkgrnd.postDelayed(bkgrndFlash, 1000);
        //try{Thread.sleep(1000);} catch (InterruptedException x){};
        return super.dispatchTouchEvent(e);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_proctoring, menu);
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
