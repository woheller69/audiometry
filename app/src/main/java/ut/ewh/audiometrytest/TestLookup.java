package ut.ewh.audiometrytest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.regex.Pattern;


public class TestLookup extends Activity {

    public final static String DESIRED_FILE = "ut.ewh.audiometrytest.DESIRED_FILE";

    public void gotoTestData(View view, String fileName){
        Intent intent = new Intent(this, TestData.class);
        intent.putExtra(DESIRED_FILE, fileName);
        startActivity(intent);
    }

    // Create table with allSavedTests.length rows; each entry will be a pressable button.
    // The button will send a packet of information to the display activity telling it what file to load

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String[] allSavedTests = fileList();
        LinearLayout layout = new LinearLayout(this);
        setContentView(layout);
        layout.setBackgroundColor(Color.parseColor("#424242"));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 16, 16, 16);

        TextView textview = new TextView(this);
        textview.setText("Audiometer Test History");
        textview.setTextColor(Color.parseColor("#FF8000"));
        textview.setTextSize(30);
        textview.setTypeface(null, Typeface.BOLD);
        textview.setGravity(Gravity.RIGHT);
        layout.addView(textview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


        if (allSavedTests.length < 2) {
            TextView message = new TextView(this);
            message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            message.setTextColor(Color.parseColor("#FFFFFF"));
            message.setBackgroundColor(Color.parseColor("#424242"));
            message.setText("Sorry! It seems there are no tests currently stored in memory. Please take a test, then return to this page to view the results.");
            layout.addView(message, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            ScrollView scrollview = new ScrollView(this);
            scrollview.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.FILL_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < allSavedTests.length; i++) {
                if (allSavedTests[i].equals("CalibrationPreferences")) {
                } else{
               //if (allSavedTests[i].matches("TestResults.*")) {
                    LinearLayout spacer = new LinearLayout(this);
                    spacer.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
                    container.addView(spacer);

                    Button b = new Button(this);
                    final int number = i;
                    b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT+50));
                    b.setBackgroundResource(R.drawable.button_background);
                    b.setPadding(10, 20, 10, 20);
                    String[] names = allSavedTests[i].split("[-]");
                    String name = "Test on " + names[1].replaceAll("\\.*_\\.*", ".") + ", " + names[1] + " Ear";
                    b.setText(name);
                    b.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            gotoTestData(view, allSavedTests[number]);
                        }
                    });
                    container.addView(b, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                }

            }
            scrollview.addView(container);
            layout.addView(scrollview);
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.test_lookup, menu);
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
