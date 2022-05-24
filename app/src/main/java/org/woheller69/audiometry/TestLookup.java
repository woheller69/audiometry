package org.woheller69.audiometry;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class TestLookup extends AppCompatActivity {

    String[] allSavedTests;
    public void gotoTestData(View view, int index){
        Intent intent = new Intent(this, TestData.class);
        intent.putExtra("Index", index);
        startActivity(intent);
    }

    // Create table with allSavedTests.length rows; each entry will be a pressable button.
    // The button will send a packet of information to the display activity telling it what file to load

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark,getTheme()));
    }
    @Override
    protected void onResume() {
        super.onResume();
        createView();
    }

    private void createView() {
        LinearLayout layout = new LinearLayout(this);
        setContentView(layout);
        layout.setBackgroundColor(getResources().getColor(R.color.background_grey,getTheme()));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 40, 16, 16);

        TextView textview = new TextView(this);
        textview.setText(R.string.title_activity_test_lookup);
        textview.setTextColor(getResources().getColor(R.color.orange,getTheme()));
        textview.setTextSize(30);
        textview.setTypeface(null, Typeface.BOLD);
        textview.setGravity(Gravity.CENTER);
        layout.addView(textview, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        allSavedTests = getAllSavedTests(this);

        if (allSavedTests.length < 1) {
            TextView message = new TextView(this);
            message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            message.setTextColor(getResources().getColor(R.color.white,getTheme()));
            message.setTextSize(20);
            message.setBackgroundColor(Color.parseColor("#424242"));
            message.setPadding(40, 30, 16, 0);
            message.setText(R.string.no_test_results);
            layout.addView(message, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        } else {
            ScrollView scrollview = new ScrollView(this);
            scrollview.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));
            LinearLayout container = new LinearLayout(this);
            container.setOrientation(LinearLayout.VERTICAL);

            for (int i = 0; i < allSavedTests.length; i++) {

                LinearLayout spacer = new LinearLayout(this);
                spacer.setLayoutParams(new LinearLayout.LayoutParams(40, 40));
                container.addView(spacer);

                Button b = new Button(this);
                b.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT + 50));
                b.setBackgroundResource(R.drawable.button_background);
                b.setPadding(10, 20, 10, 20);
                String[] names = allSavedTests[i].split("-");

                String time = DateFormat.getTimeInstance(DateFormat.SHORT).format(Long.parseLong(names[1])) + ", " + DateFormat.getDateInstance(DateFormat.SHORT).format(Long.parseLong(names[1]));
                String name = getString(R.string.test_at,time);
                b.setText(name);
                int finalI = i;
                b.setId(finalI);
                registerForContextMenu(b);
                b.setOnClickListener(view -> gotoTestData(view, finalI));
                container.addView(b, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            scrollview.addView(container);
            layout.addView(scrollview);

        }
    }

    public static String[] getAllSavedTests(Context context) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
        int user = prefManager.getInt("user",1);

        List<String> list = new ArrayList<String>(Arrays.asList(context.fileList()));
        Collections.sort(list,Collections.reverseOrder());
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
            String string = iterator.next();
            if (string.equals("CalibrationPreferences")) iterator.remove();
            else if (string.equals("Gain")) iterator.remove();
            else if (user == 1 && string.contains("U2")) iterator.remove();
            else if (user == 2 && !string.contains("U2")) iterator.remove();
        }
        return list.toArray(new String[0]);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, v.getId(), 0, getString(R.string.delete));
    }

    @Override
    public boolean onContextItemSelected(MenuItem item){
        FileOperations fileOperations = new FileOperations();
        fileOperations.deleteTestData(allSavedTests[item.getItemId()],this);
        createView();  //recreate View
        return true;
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

        return super.onOptionsItemSelected(item);
    }

}
