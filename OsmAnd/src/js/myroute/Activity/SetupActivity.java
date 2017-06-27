package js.myroute.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.IOException;

import js.myroute.Config.Singleton;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;

import js.myroute.Routing.Logic.Vertex;

public class SetupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private int l = 5;
    private Spinner environment;
    private Spinner elevation;
    private Spinner view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        readUserData();

        environment = (Spinner) findViewById(R.id.spinner_environment);
        elevation = (Spinner) findViewById(R.id.spinner_elevation);
        view = (Spinner) findViewById(R.id.spinner_view);
        Spinner activity = (Spinner) findViewById(R.id.spinner_activity);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.spinner_selection, R.layout.spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.spinner_selection_elevation, R.layout.spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this, R.array.spinner_selection, R.layout.spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(this, R.array.spinner_selection_activity, R.layout.spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        environment.setAdapter(adapter);
        environment.setOnItemSelectedListener(this);
        environment.setSelection(Singleton.getInstance().getEnvironmentImportance());
        elevation.setAdapter(adapter2);
        elevation.setOnItemSelectedListener(this);
        elevation.setSelection(Singleton.getInstance().getElevationImportance() + 1);
        view.setAdapter(adapter3);
        view.setOnItemSelectedListener(this);
        view.setSelection(Singleton.getInstance().getViewImportance());
        activity.setAdapter(adapter4);
        activity.setOnItemSelectedListener(this);
        activity.setSelection(Singleton.getInstance().getTypeOfActivity());



        view.setOnItemSelectedListener(this);
        environment.setSelection(Singleton.getInstance().getEnvironmentImportance());

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        if (Singleton.getInstance().getSameEndAsStart())
            checkBox.setChecked(true);
        else
            checkBox.setChecked(false);

        final SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        final EditText editText = (EditText) findViewById(R.id.editText);
        l = ((Singleton.getInstance().getLengthIn())/1000);
        editText.setText( String.valueOf(l) );
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String text = textView.getText().toString();
                try {
                    l = Integer.parseInt(text);
                    if (l < 1) {
                        l = Singleton.getInstance().getLengthIn()/1000;
                        editText.setText( String.valueOf(l) );
                        return true;
                    }
                    Singleton.getInstance().setLengthIn(l*1000);
                    seekBar2.setProgress(l);
                    InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    editText.clearFocus();
                } catch (NumberFormatException e) {
                    return true;
                }
                return true;
            }
        });
        seekBar2.setProgress((Singleton.getInstance().getLengthIn())/1000);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = (Singleton.getInstance().getLengthIn())/1000;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                if (fromUser) {
                    progress = progresValue;
                    l = progress;
                    editText.setText( String.valueOf(l) );
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (progress < 1) {
                    l = 1;
                    setProgress(l);
                } else
                    l = progress;
                editText.setText( String.valueOf(l) );
                Singleton.getInstance().setLengthIn(l*1000);
            }
        });
    }

    private void readUserData() {
        String data="";
        try {
            FileInputStream fin = openFileInput("LastUsedLength");
            int c;
            while( (c = fin.read()) != -1){
                data = data + Character.toString((char)c);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!data.equals("")){
            Singleton.getInstance().setLengthIn(Integer.parseInt(data));
        }

        double lat=0;
        double lon=0;
        data="";
        try {
            FileInputStream fin = openFileInput("LastUsedPosLat");
            int c;
            while( (c = fin.read()) != -1){
                data = data + Character.toString((char)c);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!data.equals("")){
            lat = Double.parseDouble(data);
        }
        data="";
        try {
            FileInputStream fin = openFileInput("LastUsedPosLon");
            int c;
            while( (c = fin.read()) != -1){
                data = data + Character.toString((char)c);
            }
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(!data.equals("")){
            lon = Double.parseDouble(data);
        }
        if(lat!=0 && lon!=0){
            Singleton.getInstance().setCurrPos(new Vertex(lat,lon));
        }
    }

    public void chooseOnMap(View view) {
        Singleton.getInstance().setUseCurrentLocation(false);
        Singleton.getInstance().setLengthIn((int)(l*1000));
        Singleton.getInstance().setSetupComplete(true);
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivity(myIntent);
        // Todo: maybe do differently?
    }

    public void useCurrentLocation(View view) {
        Singleton.getInstance().setUseCurrentLocation(true);
        Singleton.getInstance().setLengthIn((int)(l*1000));
        Singleton.getInstance().setSetupComplete(true);
        Intent myIntent = new Intent(this, MapActivity.class);
        startActivity(myIntent);
    }

    public void clickCheckboxSameEndAsStart(View view) {
        CheckBox box = (CheckBox) view;
        if (box.isChecked())
            Singleton.getInstance().setSameEndAsStart(true);
        else if (!box.isChecked())
            Singleton.getInstance().setSameEndAsStart(false);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View v, int i, long l) {
        if (adapterView.equals(findViewById(R.id.spinner_environment))) {
            Singleton.getInstance().setEnvironmentImportance(i);
        } else if (adapterView.equals(findViewById(R.id.spinner_elevation))) {
            Singleton.getInstance().setElevationImportance(i - 1);
        } else if (adapterView.equals(findViewById(R.id.spinner_view))) {
            Singleton.getInstance().setViewImportance(i);
        } else if (adapterView.equals(findViewById(R.id.spinner_activity))) {
            Singleton.getInstance().setTypeOfActivity(i);
            if (i == 3) // Hiking
                view.setSelection(2);
            else if (i == 2 || i == 4)
                elevation.setSelection(0);

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        /* do nothing */
    }
}
