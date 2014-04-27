package ut.ewh.audiometrytest;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;


public class Calibration extends ActionBarActivity {

    final private int numSamples = 3 * 44100;
    final private int bufferSize = 16384;
    final private int frequency = 1000; //in Hz
    final private int sampleRate = 44100;
    final private float increment  = (float)(Math.PI) * frequency / sampleRate;
    final private int volume = 32767;


    // final private int bufferSize = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT );
    private boolean tone = false;

    private byte[] recordAudio(){
        byte micInput[] = new byte[bufferSize];
        AudioRecord audiorecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        //creates new AudioRecord (MIC, 44100 Hz, CHANNEL_IN_MONO, ENCODING_PCM_16BIT,
        int recordResult = audiorecord.read(micInput, 0, bufferSize);
        Log.i("Recording Result: ", "The recorder says:" + recordResult);
        //Log.i("Recording Result: ", "The recorder says: nothing!");
        return micInput;
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

    public void playSound(byte[] generatedSnd) {
        //final AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        // audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        //audioTrack.flush();
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd, 0, generatedSnd.length);
        audioTrack.play();
    }
    /*public void DFT(byte[] recordedSnd) {
        int m = recordedSnd.length;
        byte outputSnd[] = new byte[recordedSnd.length];
        for (int i = 0; i < m; i++){
            outputSnd[i] = 0;
            double arg = -2*3.141592654*(double)i*(double)m;
            for (int k = 0; k < m; k++){
                double cosarg = Math.cos(k * arg);
                //double sinarg = Math.sin(k * arg);
                outputSnd[i] += recordedSnd[k] * cosarg;
            }

        }
        for (int i = 0; i < m; i++){
            recordedSnd[i] = outputSnd[i];
        }
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);
        /*tone = false;
        Thread recordingThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {};
                byte silence[] = recordAudio();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException x) {};
                tone = true;
                byte toneSample[] = recordAudio();
                //playSound(toneSample);
                Log.i("Recording ", "done! " + bufferSize);
            }
        });
        Thread toneThread = new Thread(new Runnable() {
            public void run(){
                while (tone){
                    playSound(genTone(increment, volume));
                }
            }
        });
        recordingThread.start();
        //toneThread.start();*/
        Thread calibrateThread = new Thread(new Runnable() {
           public void run() {
                MediaRecorder mediaRecorder = new MediaRecorder();
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
                mediaRecorder.setOutputFile("/sdcard/audioFile");
                mediaRecorder.setAudioSamplingRate(sampleRate);
                mediaRecorder.setAudioEncodingBitRate(64000);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                try{
                    mediaRecorder.prepare();
                } catch (IOException e) {
                    Log.i("IOException", "Failing for some other reason");
                } catch (IllegalStateException x){
                    Log.i("Illegal State Exception", "Not being initialized at the right time");
                }
                mediaRecorder.start();
                try{
                   Thread.sleep(1000);
                } catch (InterruptedException e){};
                int background = mediaRecorder.getMaxAmplitude();
                //playSound(genTone(increment, volume));
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException e){};
                int toneVolume = mediaRecorder.getMaxAmplitude();
                Log.i("Returned Volume Levels Are", "Background: " + background + " toneVolume: " + toneVolume);
               try{
                   Thread.sleep(1000);
               } catch (InterruptedException e){};
               int otherTestVolume = mediaRecorder.getMaxAmplitude();
               try{
                   Thread.sleep(1000);
               } catch (InterruptedException e){};
               int finalTestVolume = mediaRecorder.getMaxAmplitude();
               Log.i("Returned Volume Levels Are", "Background: " + otherTestVolume + " toneVolume: " + finalTestVolume);

               try{
                   Thread.sleep(1000);
               } catch (InterruptedException e){};
               int furtherTesting = mediaRecorder.getMaxAmplitude();
               try{
                   Thread.sleep(1000);
               } catch (InterruptedException e){};
               int evenFurtherTesting = mediaRecorder.getMaxAmplitude();
               Log.i("Returned Volume Levels Are", "Background: " + furtherTesting + " toneVolume: " + evenFurtherTesting);



               mediaRecorder.stop();
                mediaRecorder.reset();
                mediaRecorder.release();
           }
        });
        calibrateThread.start();
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
