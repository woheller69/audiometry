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



public class TestProctoring extends ActionBarActivity {
    private final int duration = 3;
    private final int sampleRate = 22050;
    private final int numSamples = duration * sampleRate;
    //private final byte generatedSnd[] = new byte[2 * numSamples];
    //private final byte finalSnd[] = new byte[2 * numSamples];
    private boolean heard = false;
    private boolean loop = true;
    int a = 0;
    private int volume = 32767;
    // final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);

    //View.OnTouchListener onTouchListener;
    //public GestureDetector gestureDetector;
    //private int userResponse = 0;

    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
    public final int[] testingResults = {0, 0, 0, 0, 0, 0, 0};
    public int[] thresholds = {0, 0, 0, 0, 0, 0, 0};
    public int iteration = 0;

    Runnable bkgrndFlash = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundResource(R.color.white);
            //bkgrnd.postDelayed(this, 1000);
        }
    };
    Runnable bkgrndFlashBlack = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundResource(R.color.black);
            //bkgrnd.postDelayed(this, 1000);
        }
    };
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
            //volume controlled by the value multiplied by by dVal; max value is 32767
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);
        }
        return generatedSnd;
    }
    //AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);

    public void playSound(byte[] generatedSnd) {
        //final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        // audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        //audioTrack.flush();
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_proctoring);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        Thread testThread = new Thread (new Runnable() {
            public void run(){
                for (int i = 0; i < testingFrequencies.length; i++){
                    //heard = false;
                    //TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                    iteration = i;
                    int frequency = testingFrequencies[i];
                    float increment  = (float)(Math.PI) * frequency / sampleRate;
                    int maxVolume = volume;
                    int minVolume = 0;
                    // This is the loop for each individual sample using a binary search algorithm
                    for(;;){
                        // AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
                        int tempResponse = 0;
                        int actualVolume = (minVolume + maxVolume)/2;
                        if ((maxVolume - minVolume) < 400){ //the test is done
                            thresholds[i] = actualVolume;
                            Log.i("Final Results", "results are " + thresholds[0] + " " + thresholds[1] + " " + thresholds[2] + " " + thresholds[3] + " " + thresholds[4] + " " + thresholds[5] + " " + thresholds[6]);
                            break;
                        } else { //still taking the test
                            for (int z = 0; z < 3; z++) {
                                heard = false;
                                Log.i("Playback Info", "actual volume is" + actualVolume);
                                playSound(genTone(increment, actualVolume));
                                try {
                                    Thread.sleep(3000);
                                } catch (InterruptedException e) {};
                                if (heard) {
                                    tempResponse ++;
                                }
                            }
                            if (tempResponse >= 2){
                                maxVolume = actualVolume;
                            }
                            else {
                                minVolume = actualVolume;
                            }
                        }
                    };
                    Log.i("Loop Alert", "New Frequency Beginning " + heard);

                }
                Log.i("Final Results", "results are " + thresholds[0] + " " + thresholds[1] + " " + thresholds[2] + " " + thresholds[3] + " " + thresholds[4] + " " + thresholds[5] + " " + thresholds[6]);
                loop = false;
                TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                gotoComplete();
            }
        });
        Thread checkThread = new Thread (new Runnable() {
            public void run(){
                while (loop){
                    if (heard){
                        int f = iteration;
                        testingResults[f] = 1;
                        //	TestProctoring.this.runOnUiThread(bkgrndFlash);
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
							Thread.sleep(500);
						} catch (InterruptedException x){};*/
                        while (heard){

                        };
                        TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
                    };
                };
            };
        });
        testThread.start();
        checkThread.start();
        screenThread.start();
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
