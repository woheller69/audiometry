package org.woheller69.audiometry;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Sound {
    /**
     * Generates the tone based on the increment and volume, used in inner loop
     * @param increment - the amount to increment by
     * @param volume - the volume to generate
     */
    public float[] genTone(float increment, int volume, int numSamples) {
        float angle = 0;
        float[] generatedSnd = new float[numSamples];

        int fadeInSamples = Math.max(1, numSamples / 10);
        int fadeOutSamples = Math.max(1, numSamples / 10);

        for (int i = 0; i < numSamples; i++) {
            float sampleValue = (float) (Math.sin(angle) * volume / 32768);

            if (i < fadeInSamples) {
                // Phase-in effect
                sampleValue *= (float) i / fadeInSamples;
            } else if (i >= numSamples - fadeOutSamples) {
                // Phase-out effect
                sampleValue *= (float) (numSamples - i) / fadeOutSamples;
            }

            generatedSnd[i] = sampleValue;
            angle += increment;
        }

        return generatedSnd;
    }

    /**
     * New code that avoids deprecated API except volume setting
     * Writes the parameter byte array to an AudioTrack and plays the array
     * @param generatedSnd- input PCM float array
     */
    public AudioTrack playSound(float[] generatedSnd, int ear, int sampleRate) {
        // Define the audio attributes
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        // Define the audio format
        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();

        // Calculate the buffer size in bytes
        int bufferSizeInBytes = AudioTrack.getMinBufferSize(sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_FLOAT);

        // Ensure the buffer size is at least the size of the generated sound data
        bufferSizeInBytes = Math.max(bufferSizeInBytes, generatedSnd.length * 4); // 4 bytes per float

        // Create the AudioTrack
        AudioTrack audioTrack = new AudioTrack(audioAttributes,
                audioFormat,
                bufferSizeInBytes,
                AudioTrack.MODE_STATIC,
                0);

        // Write the audio data to the AudioTrack
        audioTrack.write(generatedSnd, 0, generatedSnd.length, AudioTrack.WRITE_BLOCKING);

        // Set the volume and play the audio
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
