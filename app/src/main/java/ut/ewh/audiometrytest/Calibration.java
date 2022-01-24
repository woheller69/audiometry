package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


public class Calibration extends ActionBarActivity {

    final private int sampleRate = 44100;
    final private int numSamples = 4 * sampleRate;
    final static public int[] frequencies = {500, 1000, 3000, 4000, 6000, 8000};
    final private int volume = 30000;
    final private double mGain = 0.0044;
    final private double mAlpha = 0.9;
    final private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    double[] inputSignalImaginary = new double[2048];
    final private double[] dbHLCorrectionCoefficients = {13.5, 7.5, 11.5, 12, 16, 15.5}; //based off of ANSI Standards


    public double[] calibrationArray = new double[frequencies.length];
    public static boolean running = true;

    public void gotoCalibrationComplete(){
        Intent intent = new Intent(this, CalibrationComplete.class);
        startActivity(intent);
    }

    public byte[] genTone(float increment, int volume) {
        float angle = 0;
        double[] sample = new double[numSamples];
        byte[] generatedSnd = new byte[2 * numSamples];
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

    public int newBitReverse(int j){
        int b = 0;
        while (j!=0){
            b<<=1;
            b|=( j &1);
            j>>=1;
        }
        return b;
    }

    public double[] fftAnalysis(double[] inputReal, double[] inputImag){
        int n = 2048;
        int nu = 11;
        double[] bufferReal = new double[n];
        double[] shortenedReal =  new double[n];

        //shorten buffer data to a power of two
        for (int s = 0; s < n; s++){
            shortenedReal[s] = inputReal[s];
        }

        // Computing the coefficients for everything ahead of time
        double[][] Real = new double[nu+1][n];
        int counter = 2;
        for (int l = 1; l <= nu; l++){
            for(int i = 0; i<n; i++){
                Real[l][i] = Math.cos(((double)2)*Math.PI*((double)i)/((double)counter));
            }
            counter *= 2;
        }
        double[][] Imag = new double[nu+1][n];
        counter = 2;
        for (int l = 1; l <= nu; l++){
            for(int i = 0; i<n; i++){
                Imag[l][i] = -1*Math.sin(((double)2)*Math.PI*((double)i)/((double)counter));
            }
            counter *= 2;
        }

        // Populate bufferReal with inputReal in bit-reversed order
        for (int x = 0; x < shortenedReal.length; x ++){
            int p = newBitReverse(x);
            bufferReal[x] = shortenedReal[p];
            //Log.i("Check", "bufferReal: " + bufferReal[x] + " shortenedReal: " + shortenedReal[p]);

        }

        // begin transform
        int step = 1;
        for (int level = 1; level <= nu; level ++){
            int increm = step * 2;
            for (int j = 0; j < step; j++){
                for(int i = j; i < n; i += increm){
                    double realCoefficient = Real[level][j];
                    double imagCoefficient = Imag[level][j];
                    realCoefficient *= bufferReal[i+step];
                    imagCoefficient *= bufferReal[i+step];
                    bufferReal[i+step] = bufferReal[i];
                    inputImag[i+step] = inputImag[i];
                    bufferReal[i+step] -= realCoefficient;
                    inputImag[i+step] -= imagCoefficient;
                    bufferReal[i] += realCoefficient;
                    inputImag[i] += imagCoefficient;
                }
            }
            step *= 2;
        }
        double[] transformResult = new double[bufferReal.length];
        // Calculate magnitude of FFT coefficients
        for (int q = 0; q < bufferReal.length; q++){
            transformResult[q] = Math.sqrt(Math.pow(bufferReal[q], 2) + Math.pow(inputImag[q], 2));
        }
        return transformResult;
    }

    public double[] dbListen(int frequency) {
        double rmsArray[] = new double[5];
        for (int j = 0; j < rmsArray.length; j++) {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
            short[] buffer = new short[bufferSize];

            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {System.out.println (e.toString());
            }
            int bufferReadResult = audioRecord.read(buffer, 0, buffer.length);

            //Convert buffer from type short[] to double[]
            double[] inputSignal = new double[buffer.length];
            for(int x=0;x<buffer.length; x++){
                inputSignal[x] = (double)buffer[x];
            }

            double[] outputSignal = fftAnalysis(inputSignal, inputSignalImaginary);

            int k = frequency*2048/sampleRate; // Selects the value from the transform array corresponding to the desired frequency
            rmsArray[j] = outputSignal[k];

           // RMS Routine
            double rms = 0;
            for (int i = 0; i < buffer.length; i++) {
                rms += buffer[i] * buffer[i];
            }
//          //smoothing of rms
            //double mRmsSmoothed = (1 - mAlpha) * rms;
            // RMS Decibel Calculation

          // FFT Decibel Calculation
            audioRecord.stop();
            audioRecord.release();

        }
        //Log.i("Array Check", "rmsArray: " + rmsArray[0] + " " + rmsArray[1] + " " + rmsArray[2] + " " + rmsArray[3] + " " + rmsArray[4]);
        return rmsArray;
    }

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
                if (!running){
                    return;
                }
                double backgroundRms[] = dbListen(frequency);

                audioTrack.play();

                double soundRms[] = dbListen(frequency);

                double resultingRms[] = new double[5];
                double resultingdB[] = new double[5];

                for(int x = 0; x < resultingRms.length; x++){
                    resultingRms[x] = soundRms[x]/backgroundRms[x];
                    resultingdB[x] = 20 * Math.log10(resultingRms[x]) + 70;
                    resultingdB[x] -= dbHLCorrectionCoefficients[i];
                    Log.i("FFT Decibel", "Reading "+ resultingdB[x]);
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
                double dBAverage = 0;
                for(int q = 1; q < resultingdB.length; q++){
                    dBAverage += resultingdB[q];
                }
                dBAverage /= (resultingdB.length - 1);
                calibrationArray[i] = dBAverage / volume; // create ratio of dB/binary. Will be used in testProctoring for final conversion.
                if (!running){
                    return;
                }

                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {};

                audioTrack.release();

            }
            int counter = 0;
            byte[] calibrationByteArray = new byte[calibrationArray.length * 8];
            for (int x = 0; x < calibrationArray.length; x++){
                byte[] tmpByteArray = new byte[8];
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
                } catch (IOException q) {System.out.println (q.toString());}
            } catch (FileNotFoundException e) {System.out.println (e.toString());
            }

            gotoCalibrationComplete();


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
        }
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
