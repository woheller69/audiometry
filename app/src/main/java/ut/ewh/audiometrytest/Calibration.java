package ut.ewh.audiometrytest;

import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;


public class Calibration extends ActionBarActivity {

    final private int sampleRate = 44100;
    final private int numSamples = 4 * sampleRate;
    final static public int[] frequencies = {125, 250, 500, 1000, 3000, 4000, 6000, 8000};
    final private double[] dbHLCorrectionCoefficients = {45.0, 27.0, 13.5, 7.5, 11.5, 12, 16, 15.5}; //based off of ANSI Standards
    final private int volume = 30000;
    final private int bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
    private Context context;

    public double[] calibrationArray = new double[frequencies.length];
    public static boolean running = true;

    public void showToast(final String toast)
    {
        runOnUiThread(() -> Toast.makeText(this, toast, Toast.LENGTH_SHORT).show());
    }

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

        return audioTrack;
    }

    public double[] dbListen(int frequency) {
        double[] rmsArray = new double[5];
        for (int j = 0; j < rmsArray.length; j++) {
            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
                short[] buffer = new short[bufferSize];

                audioRecord.startRecording();
                int result = audioRecord.read(buffer, 0, buffer.length);
                Log.i("AudioRecord", Integer.toString(result));
                audioRecord.stop();
                audioRecord.release();

            //Convert buffer from type short[] to double[]
            double[] inputSignal = new double[buffer.length];
            for(int x=0;x<buffer.length; x++){
                inputSignal[x] = (double)buffer[x];
            }

            Complex[] fft_input = new Complex[2048];

            for (int i = 0; i < 2048; i++) {
                fft_input[i] = new Complex(inputSignal[i], 0);
            }

            Complex[] fft_output = FFT.fft(fft_input) ;

            int k = frequency*2048/sampleRate; // Selects the value from the transform array corresponding to the desired frequency

            //Every complex element of the complex array in the frequency domain can be considered a frequency coefficient,
            // and has a magnitude ( sqrt(R*R + I*I) ).
            rmsArray[j] = Math.sqrt(Math.pow(fft_output[k].re(),2)+Math.pow(fft_output[k].im(),2));

        }
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

                double[] backgroundRms = dbListen(frequency);
                Log.i("Array Check", "background: " + " " + backgroundRms[1] + " " + backgroundRms[2] + " " + backgroundRms[3] + " " + backgroundRms[4]);

                AudioTrack audioTrack = playSound(genTone(increment, volume));
                if (!running){
                    return;
                }
                audioTrack.play();

                double[] soundRms = dbListen(frequency);
                Log.i("Array Check", "sound: " + " " + soundRms[1] + " " + soundRms[2] + " " + soundRms[3] + " " + soundRms[4]);

                double[] resultingRms = new double[5];
                double[] resultingdB = new double[5];

                for(int x = 1; x < resultingRms.length; x++){  //do not use first measurement, sound maybe not yet running or still running
                    resultingRms[x] = soundRms[x]/backgroundRms[x]; //problem if no noise measured and backgroundRms=0
                    resultingdB[x] = 20 * Math.log10(resultingRms[x]) + 70; //why 70?
                    if (Double.isNaN(resultingdB[x])) {
                        resultingdB[x]=0;  //workaround if calibration not working
                        showToast("Error during calibration");
                    }
                    resultingdB[x] -= dbHLCorrectionCoefficients[i];
                }
                Log.i("Array Check", "Correction: "  + " " + resultingdB[1] + " " + resultingdB[2] + " " + resultingdB[3] + " " + resultingdB[4]);

                double dBAverage = 0;
                for(int q = 1; q < resultingdB.length; q++){  //do not use first measurement, sound maybe not yet running or still running
                    dBAverage += resultingdB[q];
                }
                dBAverage /= (resultingdB.length - 1);
                calibrationArray[i] = dBAverage / volume; // create ratio of dB/binary. Will be used in testProctoring for final conversion.
                Log.i("Array Check", "Calibration: " + frequencies[i] + " " + calibrationArray[i]);
                if (!running){
                    return;
                }

                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e) {};

                audioTrack.stop();
                audioTrack.release();

            }

            FileOperations fileOperations = new FileOperations();
            fileOperations.writeCalibration(calibrationArray, context);

            gotoCalibrationComplete();


        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        context=this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
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
