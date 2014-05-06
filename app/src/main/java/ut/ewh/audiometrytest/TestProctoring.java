package ut.ewh.audiometrytest;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;
import android.view.View;
import android.view.MotionEvent;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;




public class TestProctoring extends ActionBarActivity {
    private final int duration = 1;
    private final int sampleRate = 22050;
    private final int numSamples = duration * sampleRate;
    private final int volume = 32767;
    private final int[] testingFrequencies = {1000, 500, 1000, 3000, 4000, 6000, 8000};
    final private double mGain = 0.0044;
    final private double mAlpha = 0.9;


    private boolean heard = false;
    private boolean loop = true;
    int a = 0;
    public static boolean running = true;

    public double[] thresholds_right = {0, 0, 0, 0, 0, 0, 0};
    public double[] thresholds_left = {0, 0, 0, 0, 0, 0, 0};

    public static void stopThread(){
        running = false;
    }


    /**
     * Randomly picks time gap between test tones in ms
     * @return
     */
    public int randomTime(){
        int time;
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
//        Integer i = new Integer(4);
//        i.byteValue();
//        Short s = new Short(4);
//        s.

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
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        if (ear == 0) {
            audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        } else {
            audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), 0);
        }
        audioTrack.play();
    }

    public class testThread extends Thread {
        public void run() {
            byte calibrationByteData[] = new byte[48];

            try{
                FileInputStream fis = openFileInput("CalibrationPreferences");
                fis.read(calibrationByteData, 0, 48);
                fis.close();
                //Log.i("File Read Info", "File Read Successful");
            } catch (IOException e) {};

            final double calibrationArray[] = new double[6];

            int counter = 0;

            for (int i = 0; i < calibrationArray.length; i++){
                byte tmpByteBuffer[] = new byte[8];
                for (int j = 0; j < 8; j++) {
                    tmpByteBuffer[j] = calibrationByteData[counter];
                    counter++;
                }
                calibrationArray[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
            }
            //Log.i("Calibration Data", "Calibration factors are: " + calibrationArray[0] + " " + calibrationArray[1] + " " + calibrationArray[2] + " " + calibrationArray[3] + " " + calibrationArray[4] + " " + calibrationArray[5]);

            //iterated once for every frequency to be tested
            for (int s = 0; s < 2; s++) {
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
                                if (i == 0 || i == 2) {
                                    thresholds_right[i] = actualVolume * calibrationArray[1]; //records volume as threshold
                                } else if (i == 1){
                                    thresholds_right[i] = actualVolume * calibrationArray[0]; //records volume as threshold
                                } else if (i == 3) {
                                    thresholds_right[i] = actualVolume * calibrationArray[2]; //records volume as threshold
                                } else if (i == 4) {
                                    thresholds_right[i] = actualVolume * calibrationArray[3]; //records volume as threshold
                                } else if (i == 5) {
                                    thresholds_right[i] = actualVolume * calibrationArray[4]; //records volume as threshold
                                } else if (i == 6) {
                                    thresholds_right[i] = actualVolume * calibrationArray[5]; //records volume as threshold
                                } else {}
                                //Log.i("Temporary Results", "results are " + thresholds_right[0] + " " + thresholds_right[1] + " " + thresholds_right[2] + " " + thresholds_right[3] + " " + thresholds_right[4] + " " + thresholds_right[5] + " " + thresholds_right[6]);
                            } else {
                                if (i == 0 || i == 2) {
                                    thresholds_left[i] = actualVolume * calibrationArray[1]; //records volume as threshold
                                } else if (i == 1){
                                    thresholds_left[i] = actualVolume * calibrationArray[0]; //records volume as threshold
                                } else if (i == 3) {
                                    thresholds_left[i] = actualVolume * calibrationArray[2]; //records volume as threshold
                                } else if (i == 4) {
                                    thresholds_left[i] = actualVolume * calibrationArray[3]; //records volume as threshold
                                } else if (i == 5) {
                                    thresholds_left[i] = actualVolume * calibrationArray[4]; //records volume as threshold
                                } else if (i == 6) {
                                    thresholds_left[i] = actualVolume * calibrationArray[5]; //records volume as threshold
                                } else {}
                                //Log.i("Temporary Results", "results are " + thresholds_left[0] + " " + thresholds_left[1] + " " + thresholds_left[2] + " " + thresholds_left[3] + " " + thresholds_left[4] + " " + thresholds_left[5] + " " + thresholds_left[6]);

                            }
                            break; //go to next frequency
                        } else {
                            for (int z = 0; z < 3; z++) { //iterate three times per volume level
                                heard = false;
                                //Log.i("Playback Info", "actual volume is" + actualVolume);
                                if (!running){
                                    return;
                                }
                                playSound(genTone(increment, actualVolume), s);
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
                    //Log.i("Loop Alert", "New Frequency Beginning " + heard);

                }
                //Log.i("Final Results", "results are " + thresholds[0] + " " + thresholds[1] + " " + thresholds[2] + " " + thresholds[3] + " " + thresholds[4] + " " + thresholds[5] + " " + thresholds[6]);
                TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
            }
            loop = false;

            double thresholdVolumeRight[] = new double[thresholds_right.length];
            double thresholdVolumeLeft[] = new double[thresholds_left.length];

            for (int i = 0; i < thresholds_right.length; i++) {
                thresholdVolumeRight[i] = 10 * Math.log10(mGain * thresholds_right[i]);
            }
            for (int i = 0; i < thresholds_left.length; i++) {
                thresholdVolumeLeft[i] = 10 * Math.log10(mGain * thresholds_left[i]);
            }
            //Log.i("Final Results Right", "results are " + thresholdVolumeRight[0] + " " + thresholdVolumeRight[1] + " " + thresholdVolumeRight[2] + " " + thresholdVolumeRight[3] + " " + thresholdVolumeRight[4] + " " + thresholdVolumeRight[5] + " " + thresholdVolumeRight[6]);
            //Log.i("Final Results", "results are " + thresholdVolumeLeft[0] + " " + thresholdVolumeLeft[1] + " " + thresholdVolumeLeft[2] + " " + thresholdVolumeLeft[3] + " " + thresholdVolumeLeft[4] + " " + thresholdVolumeLeft[5] + " " + thresholdVolumeLeft[6]);

            counter = 0;
            byte thresholdVolumeRightbyte[] = new byte[thresholdVolumeRight.length * 8];
            for (int x = 0; x < thresholdVolumeRight.length; x++){
                byte tmpByteArray[] = new byte[8];
                ByteBuffer.wrap(tmpByteArray).putDouble(thresholdVolumeRight[x]);
                for (int j = 0; j < 8; j++){
                    thresholdVolumeRightbyte[counter] = tmpByteArray[j];
                    counter++;
                }

            }
            try{
                FileOutputStream fos = openFileOutput("TestResultsRight", Context.MODE_PRIVATE);
                try{
                    fos.write(thresholdVolumeRightbyte);
                    fos.close();
                    //Log.i("Write Status", "Write Successful");
                } catch (IOException q) {}
            } catch (FileNotFoundException e) {
                //Log.e("ERROR", "Problem writing to file");
            }

            counter = 0;
            byte thresholdVolumeLeftbyte[] = new byte[thresholdVolumeLeft.length * 8];
            for (int x = 0; x < thresholdVolumeLeft.length; x++){
                byte tmpByteArray[] = new byte[8];
                ByteBuffer.wrap(tmpByteArray).putDouble(thresholdVolumeLeft[x]);
                for (int j = 0; j < 8; j++){
                    thresholdVolumeLeftbyte[counter] = tmpByteArray[j];
                    counter++;
                }

            }
            try{
                FileOutputStream fos = openFileOutput("TestResultsLeft", Context.MODE_PRIVATE);
                try{
                    fos.write(thresholdVolumeLeftbyte);
                    fos.close();
                    //Log.i("Write Status", "Write Successful");
                } catch (IOException q) {}
            } catch (FileNotFoundException e) {
               // Log.e("ERROR", "Problem writing to file");
            }


            gotoComplete();

        }
    };


    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_proctoring);

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 9,  0);

        Thread timingThread = new Thread (new Runnable() {
            public void run() {
                while (loop) {
                    if (!running){
                        return;
                    }
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
                    if (!running){
                        return;
                    }
                    if (heard){
                        TestProctoring.this.runOnUiThread(bkgrndFlash);
                        while (heard){

                        };
                    };
                };
            };
        });
        //testThread.start();
        Thread testRunningThread = new Thread(new Runnable(){
            public void run() {
                final testThread testThread = new testThread();
                testThread.run();
            }
        });
        testRunningThread.start();
        screenThread.start();
        timingThread.start();
    }



    @Override
    public boolean dispatchTouchEvent(MotionEvent e){
        Log.i("Touch Alert", "Screen was hit!" + a++ + heard);
        heard = true;
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

    @Override
    public void onStop(){
        super.onStop();
        stopThread();
    }

}
