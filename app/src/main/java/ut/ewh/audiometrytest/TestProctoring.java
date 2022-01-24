package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;


public class TestProctoring extends ActionBarActivity {
    private final int duration = 1;
    private final int sampleRate = 44100;
    private final int numSamples = duration * sampleRate;
    private final int volume = 32767;
    static public final int[] testFrequencies = {125, 250, 500, 1000, 3000, 4000, 6000, 8000};
    private boolean heard = false;
    private boolean loop = true;
    int a = 0;
    public static boolean running = true;
    public double[] thresholds_right = new double[testFrequencies.length];
    public double[] thresholds_left = new double[testFrequencies.length];
    public static void stopThread(){
        running = false;
    }


    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(TestProctoring.this, toast, Toast.LENGTH_SHORT).show());
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
            time = 2500;
        }else{
            time = 3000;
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
            view.setBackgroundColor(Color.parseColor("#adce49"));
        }
    };
    /**
     * Changes background color to black when called
     */
    Runnable bkgrndFlashBlack = new Runnable() {
        @Override
        public void run(){
            View view = findViewById(R.id.page);
            view.setBackgroundColor(Color.parseColor("#424242"));
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
        double[] sample = new double[numSamples];
        byte[] generatedSnd = new byte[2 * numSamples];
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
    public AudioTrack playSound(byte[] generatedSnd, int ear) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        if (ear == 0) {
            audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        } else {
            audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), 0);
        }
        audioTrack.play();
        return audioTrack;
    }

    public class testThread extends Thread {
        final double[] calibrationArray = new double[Calibration.frequencies.length];

        private double getCalibration(int frequency){
            for (int i=0; i<Calibration.frequencies.length; i++){
                if (frequency==Calibration.frequencies[i]){
                    return calibrationArray[i];
                }
            }
            return 0;
        }

        public void run() {
            byte[] calibrationByteData = new byte[8*calibrationArray.length];
            try{
                FileInputStream fis = openFileInput("CalibrationPreferences");
                fis.read(calibrationByteData, 0, 8*calibrationArray.length);
                fis.close();
            } catch (IOException e) {};

            int counter = 0;

            for (int i = 0; i < calibrationArray.length; i++){
                byte[] tmpByteBuffer = new byte[8];
                for (int j = 0; j < 8; j++) {
                    tmpByteBuffer[j] = calibrationByteData[counter];
                    counter++;
                }
                calibrationArray[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
            }

            //iterated once for every frequency to be tested
            for (int s = 0; s < 2; s++) {
                for (int i = 0; i < testFrequencies.length; i++) {
                    int frequency = testFrequencies[i];
                    float increment = (float) (Math.PI) * frequency / sampleRate;
                    int maxVolume = volume;
                    int minVolume = 0;
                    // This is the loop for each individual sample using a binary search algorithm
                    for (; ; ) {
                        int tempResponse = 0;
                        int actualVolume = (minVolume + maxVolume) / 2;
                        //showToast(frequency + " " + actualVolume);
                        //showToast(Double.toString(getCalibration(frequency)));
                        if (minVolume > 0 && ((float) maxVolume/ (float) minVolume) < Math.sqrt(2)) {
                            if (s==0){
                                thresholds_right[i] = actualVolume * getCalibration(frequency); //records volume as threshold
                            }else{
                                thresholds_left[i] = actualVolume * getCalibration(frequency); //records volume as threshold
                            }
                            break; //go to next frequency
                        } else {
                            for (int z = 0; z < 3; z++) { //iterate three times per volume level
                                heard = false;
                                if (!running){
                                    return;
                                }
                                AudioTrack audioTrack = playSound(genTone(increment, actualVolume), s);
                                try {
                                    Thread.sleep(randomTime());
                                } catch (InterruptedException e) {}
                                audioTrack.release();
                                if (heard) {
                                    tempResponse++;
                                }
//                                // Checks if the first two test were positive, and skips the third if true. Helps speed the test along.
                                if (tempResponse >= 2){
                                    break;
                                }
                                // Check if the first two tests were misses, and skips the third if this is the case.
                                if (z == 1 && tempResponse == 0){
                                    break;
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
                }
                TestProctoring.this.runOnUiThread(bkgrndFlashBlack);
            }
            loop = false;

            SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_yyyy-HHmmss");
            String currentDateTime = sdf.format(new Date());

            counter = 0;

            byte[] thresholdVolumeRightbyte = new byte[thresholds_right.length * 8];
            for (double v : thresholds_right) {
                byte[] tmpByteArray = new byte[8];
                ByteBuffer.wrap(tmpByteArray).putDouble(v);
                for (int j = 0; j < 8; j++) {
                    thresholdVolumeRightbyte[counter] = tmpByteArray[j];
                    counter++;
                }

            }
            try{
                FileOutputStream fos = openFileOutput("TestResults-Right-" + currentDateTime, Context.MODE_PRIVATE);
                try{
                    fos.write(thresholdVolumeRightbyte);
                    fos.close();
                } catch (IOException q) {}
            } catch (FileNotFoundException e) {}

            counter = 0;

            byte[] thresholdVolumeLeftbyte = new byte[thresholds_left.length * 8];
            for (double v : thresholds_left) {
                byte[] tmpByteArray = new byte[8];
                ByteBuffer.wrap(tmpByteArray).putDouble(v);
                for (int j = 0; j < 8; j++) {
                    thresholdVolumeLeftbyte[counter] = tmpByteArray[j];
                    counter++;
                }

            }
            try{
                FileOutputStream fos = openFileOutput("TestResults-Left-" + currentDateTime, Context.MODE_PRIVATE);
                try{
                    fos.write(thresholdVolumeLeftbyte);
                    fos.close();
                } catch (IOException q) {}
            } catch (FileNotFoundException e) {}
            gotoComplete();
        }
    }


    //--------------------------------------------------------------------------
    //End of Variable and Method Definitions
    //--------------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_proctoring);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }

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
                        } catch (InterruptedException x) {}
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

                        }
                    }
                }
            }
        });
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
