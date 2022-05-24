package org.woheller69.audiometry;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.preference.PreferenceManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.woheller69.audiometry.PerformTest.testFrequencies;

public class FileOperations {

    public static boolean isCalibrated(Context context){
        List<String> list = new ArrayList<String>(Arrays.asList(context.fileList()));
        return list.contains("CalibrationPreferences");
    }

    public double read0dBSPL(Context context){  //0dB SPL equals hearing threshold at 1000Hz
        for (int i=0; i<testFrequencies.length;i++){
            if(testFrequencies[i]==1000) return readCalibration(context)[i];
        }
        return 0;
    }

    public static void writeGain(Context context){
        try{
            FileOutputStream fos = context.openFileOutput("Gain", Context.MODE_PRIVATE);
            try{
                fos.write(PerformTest.gain);
                fos.close();
            } catch (IOException q) {System.out.println (q.toString());}
        } catch (FileNotFoundException e) {System.out.println (e.toString());
        }
    }

    public static int readGain(Context context){
        try{
            FileInputStream fis = context.openFileInput("Gain");
            int gain=fis.read();
            fis.close();
            return gain;
        } catch (IOException e) {}
        return PerformTest.defaultGain;  // return default gain
    }

    public static void deleteAllFiles(Context context){
        File file = new File(context.getFilesDir()+"/");
        for(File tempFile : file.listFiles()) {
            tempFile.delete();
        }
    }

    public void deleteCalibration(Context context){
        File file = new File(context.getFilesDir()+"/" + "CalibrationPreferences");
        file.delete();
    }

    public void writeCalibration(double[] calibrationArray, Context context) {
        double numCalibrations=0;
        double[] calibrationArrayOld = new double[testFrequencies.length+1];
        if (isCalibrated(context)) {
            calibrationArrayOld=readCalibration(context);
            numCalibrations = calibrationArrayOld[testFrequencies.length];
        }
        if (numCalibrations>0){
            for(int i=0;i<testFrequencies.length;i++){
                calibrationArray[i]=(calibrationArray[i]+calibrationArrayOld[i]*numCalibrations)/(numCalibrations+1);
            }
        }
        numCalibrations++;
        calibrationArray[testFrequencies.length]=numCalibrations;

        int counter = 0;
        byte[] calibrationByteArray = new byte[calibrationArray.length * 8]; //
        for (int x = 0; x < calibrationArray.length; x++){
            byte[] tmpByteArray = new byte[8];
            ByteBuffer.wrap(tmpByteArray).putDouble(calibrationArray[x]);
            for (int j = 0; j < 8; j++){
                calibrationByteArray[counter] = tmpByteArray[j];
                counter++;
            }

        }
        try{
            FileOutputStream fos = context.openFileOutput("CalibrationPreferences", Context.MODE_PRIVATE);
            try{
                fos.write(calibrationByteArray);
                fos.close();
            } catch (IOException q) {System.out.println (q.toString());}
        } catch (FileNotFoundException e) {System.out.println (e.toString());
        }
    }

    public int readNumCalibrations(Context context){
        double[] calibrationArray;
        calibrationArray=readCalibration(context);
        return (int) calibrationArray[testFrequencies.length];
    }

    public double[] readCalibration(Context context) {
        double[] calibrationArray = new double[testFrequencies.length+1];
        byte[] calibrationByteData = new byte[8*calibrationArray.length];
        try{
            FileInputStream fis = context.openFileInput("CalibrationPreferences");
            fis.read(calibrationByteData, 0, 8*calibrationArray.length);
            fis.close();
        } catch (IOException e) {}

        int counter = 0;

        for (int i = 0; i < calibrationArray.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = calibrationByteData[counter];
                counter++;
            }
            calibrationArray[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
            Log.i("Array Check", "Calibration: " + i + " " + calibrationArray[i]);
        }
        return calibrationArray;
    }

    public void writeTestResult(double[] thresholds_right, double[] thresholds_left, Context context) {
        int counter;
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
        String user = Integer.toString(prefManager.getInt("user",1));

        String currentDateTime = Long.toString(System.currentTimeMillis());

        counter = 0;

        byte[] thresholdVolume = new byte[thresholds_right.length * 8 + thresholds_left.length * 8];

        for (double v : thresholds_right) {
            byte[] tmpByteArray = new byte[8];
            ByteBuffer.wrap(tmpByteArray).putDouble(v);
            for (int j = 0; j < 8; j++) {
                thresholdVolume[counter] = tmpByteArray[j];
                counter++;
            }
        }
        for (double v : thresholds_left) {
            byte[] tmpByteArray = new byte[8];
            ByteBuffer.wrap(tmpByteArray).putDouble(v);
            for (int j = 0; j < 8; j++) {
                thresholdVolume[counter] = tmpByteArray[j];
                counter++;
            }
        }

        try{
            FileOutputStream fos = context.openFileOutput("TestResultsU"+ user + "-" + currentDateTime, Context.MODE_PRIVATE);
            try{
                fos.write(thresholdVolume);
                fos.close();
            } catch (IOException q) {}
        } catch (FileNotFoundException e) {}

    }

    public double[][] readTestData(String fileName, Context context) {
        byte[] testResultsByte = new byte[testFrequencies.length*8+testFrequencies.length*8];

        try{
            FileInputStream fis = context.openFileInput(fileName);
            fis.read(testResultsByte, 0, testResultsByte.length);
            fis.close();
        } catch (IOException e) {}

        double[][] testResults= new double[2][testFrequencies.length];    //left=1, right=0

        int counter = 0;

        for (int i = 0; i < testFrequencies.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsByte[counter];
                counter++;
            }
            testResults[0][i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }

        for (int i = 0; i < testFrequencies.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsByte[counter];
                counter++;
            }
            testResults[1][i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }
        return testResults;
    }

    public void deleteTestData(String fileName, Context context){
        File file = new File(context.getFilesDir()+"/" + fileName);
        file.delete();
    }

}
