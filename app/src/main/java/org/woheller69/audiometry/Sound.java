package org.woheller69.audiometry;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
    /**
     * Generates the tone based on the increment and volume, used in inner loop
     * @param increment - the amount to increment by
     * @param volume - the volume to generate
     */
    public float[] genTone(float increment, int volume, int numSamples){

        float angle = 0;
        float[] generatedSnd = new float[numSamples];
        for (int i = 0; i < numSamples; i++){
            generatedSnd[i] = (float) (Math.sin(angle)*volume/32768);
            angle += increment;
        }
        return generatedSnd;
    }

    /**
     * Writes the parameter byte array to an AudioTrack and plays the array
     * @param generatedSnd- input PCM float array
     */
    public AudioTrack playSound(float[] generatedSnd, int ear, int sampleRate) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd,0,generatedSnd.length,AudioTrack.WRITE_BLOCKING);
        if (ear == 0) {
            audioTrack.setStereoVolume(0, AudioTrack.getMaxVolume());
        } else {
            audioTrack.setStereoVolume(AudioTrack.getMaxVolume(), 0);
        }
        audioTrack.play();
        return audioTrack;
    }

    /**
     * Generates the tone based on the increment and volume, used in inner loop
     * @param increment - the amount to increment by
     * @param volume - the volume to generate
     */
    public float[] genStereoTone(float increment, int volume, int numSamples, int ear){

        float angle = 0;
        float[] generatedSnd = new float[2*numSamples];
        for (int i = 0; i < numSamples; i=i+2){
            if (ear == 0) {
                generatedSnd[i] = (float) (Math.sin(angle)*volume/32768);
                generatedSnd[i+1] = 0;
            } else {
                generatedSnd[i] = 0;
                generatedSnd[i+1] = (float) (Math.sin(angle)*volume/32768);
            }
            angle += increment;
        }
        return generatedSnd;
    }

    /**
     * Writes the parameter byte array to an AudioTrack and plays the array
     * @param generatedSnd- input PCM float array
     * PROBLEM: On some devices sound from one stereo channel (as created by genStereoSound) can be heard on the other channel
     */
    public AudioTrack playStereoSound(float[] generatedSnd, int sampleRate) {
        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_FLOAT, generatedSnd.length, AudioTrack.MODE_STATIC);
        audioTrack.write(generatedSnd,0,generatedSnd.length,AudioTrack.WRITE_BLOCKING);
        audioTrack.setVolume(AudioTrack.getMaxVolume());
        audioTrack.play();
        return audioTrack;
    }
}
