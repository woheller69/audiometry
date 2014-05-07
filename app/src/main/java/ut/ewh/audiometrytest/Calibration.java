package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class Calibration extends ActionBarActivity {

    final private int sampleRate = 44100;
    final private int numSamples = 4 * sampleRate;
    //final private int bufferSize = 16384;
    final private int frequencies[] = {500, 1000, 3000, 4000, 6000, 8000};
    //final private int frequency = 2000; //in Hz
    final private int volume = 30000;
    final private double mGain = 0.0044;
    final private double mAlpha = 0.9;
    final private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);


    public double calibrationArray[] = new double[frequencies.length];
    public static boolean running = true;

    public void gotoCalibrationComplete(){
        Intent intent = new Intent(this, CalibrationComplete.class);
        startActivity(intent);
    }

    public byte[] genTone(float increment, int volume) {
        float angle = 0;
        double sample[] = new double[numSamples];
        byte generatedSnd[] = new byte[2 * numSamples];
        for (int i = 0; i < numSamples; i++) {
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

    public AudioTrack playSound(byte[] generatedSnd) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        //audioTrack.play();
        return audioTrack;
    }

    public double[] dbListen() {
        double rmsArray[] = new double[5];
        for (int j = 0; j < rmsArray.length; j++) {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            short[] buffer = new short[bufferSize];
            //short[] buffer = new short[bufferSize * 2];

            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
               // Log.e("Recording didn't work!", e.toString());
            }
            int bufferReadResult = audioRecord.read(buffer, 0, buffer.length);
            //Log.i("Status of Buffer Read", "Result: " + bufferReadResult);

            double rms = 0;
            for (int i = 0; i < buffer.length; i++) {
                rms += buffer[i] * buffer[i];
            }

            //smoothing of rms
            rmsArray[j] = (1 - mAlpha) * rms;
            double mRmsSmoothed = (1 - mAlpha) * rms;
            double rmsdB = 10 * Math.log10(mGain * mRmsSmoothed);
            //Log.i("BKGRND:", "Decibel calculation is: " + rmsdB);
            audioRecord.stop();
            audioRecord.release();

//            try{
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {};
        }
        return rmsArray;
    }

//    final private Thread playTone = new Thread(new Runnable(){
//        public void run(){
//            AudioTrack audioTrack = playSound(genTone(increment, volume));
//            audioTrack.play();
//        }
//    });

    //public final Thread calibrateThread = new Thread(new Runnable() {
    static void stopThread(){
        running = false;
    }

    public class calibrateThread extends Thread {


        public void run() {
            running = true;
            for (int i = 0; i < frequencies.length; i++) {
                int frequency = frequencies[i];
                final float increment = (float) (Math.PI) * frequency / sampleRate;


                AudioTrack audioTrack = playSound(genTone(increment, volume));
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {};

                double backgroundRms[] = dbListen();

                audioTrack.play();

                double soundRms[] = dbListen();

                double resultingRms[] = new double[5];

                for (int j = 0; j < 5; j++) {
                    resultingRms[j] = soundRms[j] - backgroundRms[j];

                }
                double rmsSum = 0;
                int numCounter = 0;
                for (int j = 0; j < 5; j++) {
                    if (resultingRms[j] > 0) {
                        rmsSum += resultingRms[j];
                        numCounter ++;
                    } else {

                    }
                }
                calibrationArray[i] = rmsSum / (volume * numCounter);
                if (!running){
                    return;
                }


                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {};

            }
            //Log.i("Calibration Results", "Calibration factors are: " + calibrationArray[0] + " " + calibrationArray[1] + " " + calibrationArray[2] + " " + calibrationArray[3] + " " + calibrationArray[4] + " " + calibrationArray[5]);
            int counter = 0;
            byte calibrationByteArray[] = new byte[calibrationArray.length * 8];
            for (int x = 0; x < calibrationArray.length; x++){
                byte tmpByteArray[] = new byte[8];
                ByteBuffer.wrap(tmpByteArray).putDouble(calibrationArray[x]);
                for (int j = 0; j < 8; j++){
                    calibrationByteArray[counter] = tmpByteArray[j];
                    counter++;
                }

            }
            try{
                FileOutputStream fos = openFileOutput("CalibrationPreferences", Context.MODE_PRIVATE);
                try{
                    fos.write(calibrationByteArray);
                    fos.close();
                } catch (IOException q) {}
            } catch (FileNotFoundException e) {
            }

            gotoCalibrationComplete();


        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 9 ,  0);
        Thread runningThread = new Thread(new Runnable() {

            public void run() {
                final calibrateThread calibrateThread = new Calibration.calibrateThread();
                calibrateThread.run();
            }
        });
        runningThread.start();



    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calibration, menu);
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
