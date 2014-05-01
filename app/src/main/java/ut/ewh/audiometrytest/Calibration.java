package ut.ewh.audiometrytest;

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

import java.io.IOException;


public class Calibration extends ActionBarActivity {

    final private int numSamples = 4 * 44100;
    //final private int bufferSize = 16384;
    final private int frequency = 2000; //in Hz
    final private int sampleRate = 44100;
    final private float increment  = (float)(Math.PI) * frequency / sampleRate;
    final private int volume = 30000;
    final private double mGain = 0.0044;
    final private double mAlpha = 0.9;
    final private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);

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

    public void playSound(byte[] generatedSnd) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }

    public double[] dbListen() {
        double rmsArray[] = new double[5];
        for (int j = 0; j < rmsArray.length; j++) {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            short[] buffer = new short[bufferSize];
            //short[] buffer = new short[bufferSize * 2];

            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                Log.e("Recording didn't work!", e.toString());
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
            Log.i("BKGRND:", "Decibel calculation is: " + rmsdB);
            audioRecord.stop();
            audioRecord.release();

//            try{
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {};
        }
        return rmsArray;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        final Thread playTone = new Thread(new Runnable(){
            public void run(){
                playSound(genTone(increment, volume));
            }
        });

        Thread calibrateThread = new Thread(new Runnable() {
           public void run() {
               AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
               am.setStreamVolume(AudioManager.STREAM_MUSIC, 0,  0);

               double backgroundRms[] = dbListen();

               //playTone.start();

               double soundRms[] = dbListen();

               double resultingRms[] = new double[3];

               for (int j = 0; j < 3; j++){
                   resultingRms[j] = soundRms[j] - backgroundRms[j];
               }

               for (int j = 0; j < 3; j++) {
                   double rmsdB = 10 * Math.log10(mGain * resultingRms[j]);
                   Log.i("End Result:", "Decibel calculation is: " + rmsdB);
               }


           }
        });
        calibrateThread.start();
        /*AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0,  0);

        double backgroundRms[] = dbListen();

        calibrateThread.start();

        double soundRms[] = dbListen();

        double resultingRms[] = new double[3];

        for (int j = 0; j < 3; j++){
            resultingRms[j] = soundRms[j] - backgroundRms[j];
        }

        for (int j = 0; j < 3; j++) {
            double rmsdB = 10 * Math.log10(mGain * resultingRms[j]);
            Log.i("End Result:", "Decibel calculation is: " + rmsdB);
        }*/
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

}
