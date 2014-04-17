package ut.ewh.audiometrytest;

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
import java.io.IOException;

import android.widget.EditText;
import android.widget.Button;


public class ExportData extends ActionBarActivity {
    Button doneButton;
    EditText email_field;
    String email = "no email has yet been entered into this ridiculously long initialized field";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
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
