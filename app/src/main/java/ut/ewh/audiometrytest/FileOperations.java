package ut.ewh.audiometrytest;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static ut.ewh.audiometrytest.TestProctoring.testFrequencies;

public class FileOperations {

    public void writeCalibration(double[] calibrationArray, Context context) {
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
            FileOutputStream fos = context.openFileOutput("CalibrationPreferences", Context.MODE_PRIVATE);
            try{
                fos.write(calibrationByteArray);
                fos.close();
            } catch (IOException q) {System.out.println (q.toString());}
        } catch (FileNotFoundException e) {System.out.println (e.toString());
        }
    }

    public void readCalibration(double[] calibrationArray, Context context) {
        byte[] calibrationByteData = new byte[8*calibrationArray.length];
        try{
            FileInputStream fis = context.openFileInput("CalibrationPreferences");
            fis.read(calibrationByteData, 0, 8*calibrationArray.length);
            fis.close();
        } catch (IOException e) {}
        ;

        int counter = 0;

        for (int i = 0; i < calibrationArray.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = calibrationByteData[counter];
                counter++;
            }
            calibrationArray[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
            Log.i("Array Check", "Calibration: " + Calibration.frequencies[i] + " " + calibrationArray[i]);
        }
    }

    public void writeTestResult(double[] thresholds_right, double[] thresholds_left, Context context) {
        int counter;
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
            FileOutputStream fos = context.openFileOutput("TestResults-Right-" + currentDateTime, Context.MODE_PRIVATE);
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
            FileOutputStream fos = context.openFileOutput("TestResults-Left-" + currentDateTime, Context.MODE_PRIVATE);
            try{
                fos.write(thresholdVolumeLeftbyte);
                fos.close();
            } catch (IOException q) {}
        } catch (FileNotFoundException e) {}
    }

    public double[][] readTestData(String fileName, String fileNameLeft, Context context) {
        byte[] testResultsRightByte = new byte[testFrequencies.length*8];

        try{
            FileInputStream fis = context.openFileInput(fileName);
            fis.read(testResultsRightByte, 0, testResultsRightByte.length);
            fis.close();
        } catch (IOException e) {}
        ;

        byte[] testResultsLeftByte = new byte[testFrequencies.length*8];

        try{
            FileInputStream fis = context.openFileInput(fileNameLeft);
            fis.read(testResultsLeftByte, 0, testResultsLeftByte.length);
            fis.close();
        } catch (IOException e) {}
        ;


        double[][] testResults= new double[2][testFrequencies.length];    //left=0, right=1

        int counter = 0;

        for (int i = 0; i < testFrequencies.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsRightByte[counter];
                counter++;
            }
            testResults[0][i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }

        counter = 0;

        for (int i = 0; i < testFrequencies.length; i++){
            byte[] tmpByteBuffer = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsLeftByte[counter];
                counter++;
            }
            testResults[1][i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }
        return testResults;
    }

    public void deleteTestData(String fileName, Context context){
        String[] names = fileName.split("-");
        String deleteFileRight = names[0] + "-Right-" + names[2] + "-" + names[3];
        String deleteFileLeft = names[0] + "-Left-" + names[2] + "-" + names[3];
        File file = new File(context.getFilesDir()+"/" + deleteFileRight);
        file.delete();
        file = new File(context.getFilesDir()+"/" + deleteFileLeft);
        file.delete();

    }

}
