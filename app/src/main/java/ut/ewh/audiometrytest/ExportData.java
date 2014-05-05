package ut.ewh.audiometrytest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import android.widget.EditText;
import android.widget.Button;


public class ExportData extends ActionBarActivity {
    Button doneButton;
    EditText email_field;
    String email = "no email has yet been entered into this ridiculously long initialized field";

    public void gotoExportComplete() {
        Intent intent = new Intent(this, ExportComplete.class);
        startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);

        byte testResultsRightByte[] = new byte[7*8];

        try{
            FileInputStream fis = openFileInput("TestResultsRight");
            fis.read(testResultsRightByte, 0, testResultsRightByte.length);
            fis.close();
            Log.i("File Read Info", "File Read Successful");
        } catch (IOException e) {};

        Log.i("Information", "Byte Array Length (should be 56): " + testResultsRightByte.length);

        final double testResultsRight[] = new double[7];


        int counter = 0;

        for (int i = 0; i < testResultsRight.length; i++){
            byte tmpByteBuffer[] = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsRightByte[counter];
                counter++;
            }
            testResultsRight[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }
        Log.i("Calibration Data", "Calibration factors are: " + testResultsRight[0] + " " + testResultsRight[1] + " " + testResultsRight[2] + " " + testResultsRight[3] + " " + testResultsRight[4] + " " + testResultsRight[5] + " " + testResultsRight[6]);

        byte testResultsLeftByte[] = new byte[7 * 8];

        try{
            FileInputStream fis = openFileInput("TestResultsLeft");
            fis.read(testResultsLeftByte, 0, testResultsLeftByte.length);
            fis.close();
            Log.i("File Read Info", "File Read Successful");
        } catch (IOException e) {};

        Log.i("Information", "Byte Array Length (should be 56): " + testResultsLeftByte.length);

        final double testResultsLeft[] = new double[7];


        counter = 0;

        for (int i = 0; i < testResultsLeft.length; i++){
            byte tmpByteBuffer[] = new byte[8];
            for (int j = 0; j < 8; j++) {
                tmpByteBuffer[j] = testResultsLeftByte[counter];
                counter++;
            }
            testResultsLeft[i] = ByteBuffer.wrap(tmpByteBuffer).getDouble();
        }
        Log.i("Calibration Data", "Calibration factors are: " + testResultsLeft[0] + " " + testResultsLeft[1] + " " + testResultsLeft[2] + " " + testResultsLeft[3] + " " + testResultsLeft[4] + " " + testResultsLeft[5] + " " + testResultsLeft[6]);



        doneButton = (Button)findViewById(R.id.doneButton);
        email_field = (EditText)findViewById(R.id.email_address);
        /**
         * Takes entered email address and sends test results to that email
         */
        doneButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                email = email_field.getText().toString();
                try{
                    // super.onCreate(savedInstanceState);
                    //setContentView(R.layout.activity_export_data);
                    HttpClient httpclient = new DefaultHttpClient();
                    String URL = "http://107.170.226.198/mail.php?";
                    URL += "t="+ email;
                    URL += "&s="+"test";
                    URL += "&b="+"Thresholds:+Right+Ear+[1000+Hz]+" + testResultsRight[0] +"+[500+Hz]+" + testResultsRight[1] + "+[1000+Hz+Repeated]+" + testResultsRight[2] + "+[3000+Hz]+" + testResultsRight[3] + "+[4000+Hz]+" + testResultsRight[4] +"+[6000+Hz]+" + testResultsRight[5] + "+[8000+Hz]+" + testResultsRight[6] + ".+Left+Ear+[1000+Hz]+"+ testResultsLeft[0] +"+[500+Hz]+" + testResultsLeft[1] + "+[1000+Hz+Repeated]+" + testResultsLeft[2] + "+[3000+Hz]+" + testResultsLeft[3] + "+[4000+Hz]+" + testResultsLeft[4] +"+[6000+Hz]+" + testResultsLeft[5] + "+[8000+Hz]+" + testResultsLeft[6];
                    //URL += "&b="+"two+words";
                    HttpResponse response = httpclient.execute(new HttpGet(URL));
                    StatusLine statusLine = response.getStatusLine();
                    if (statusLine.getStatusCode() == HttpStatus.SC_OK){
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        response.getEntity().writeTo(out);
                        out.close();
                        String responseString = out.toString();
                        Log.i("Connection", "Successful");
                        Log.i("connection", responseString);
                    }  else {
                        Log.i("connection", "unsuccessful");
                        response.getEntity().getContent().close();
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                    gotoExportComplete();
                } catch (Exception e){
                    Log.i("Not Good", "Not Pinging IP");
                    System.err.println(e);
                }

            }
        });
        /*try{
           // super.onCreate(savedInstanceState);
            //setContentView(R.layout.activity_export_data);
            HttpClient httpclient = new DefaultHttpClient();
            String URL = "http://107.170.226.198/mail.php?";
            URL += "t="+email;
            URL += "&s="+"test";
            URL += "&b="+"body";
            HttpResponse response = httpclient.execute(new HttpGet(URL));
            StatusLine statusLine = response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                String responseString = out.toString();
                Log.i("Connection", "Successful");
                Log.i("connection", responseString);
            }  else {
                Log.i("connection", "unsuccessful");
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (Exception e){
            Log.i("Not Good", "Not Pinging IP");
            System.err.println(e);
        }*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.export_data, menu);
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
