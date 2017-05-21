package js.myroute.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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

    private double l = 2.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        readUserData();

        Spinner environment = (Spinner) findViewById(R.id.spinner_environment);
        Spinner elevation = (Spinner) findViewById(R.id.spinner_elevation);
        Spinner view = (Spinner) findViewById(R.id.spinner_view);
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

        SeekBar seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        final TextView textView5 = (TextView) findViewById(R.id.textView5);
        l = (2.0 + (Singleton.getInstance().getLengthIn()-2000)/1000.0);
        textView5.setText( l + " km");
        seekBar2.setProgress((Singleton.getInstance().getLengthIn()-2000)/100);
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = (Singleton.getInstance().getLengthIn()-2000)/100;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue, boolean fromUser) {
                progress = progresValue;
                l = (2.0 + progress/10.0);
                textView5.setText( l + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                l = (2.0 + progress/10.0);
                textView5.setText( l + " km");
                Singleton.getInstance().setLengthIn((int)(l*1000));
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
        // Todo: put additional input parameters
        startActivity(myIntent);
        // Todo: maybe do differently?
    }

    public void useCurrentLocation(View view) {
        Singleton.getInstance().setUseCurrentLocation(true);
        Singleton.getInstance().setLengthIn((int)(l*1000));
        Singleton.getInstance().setSetupComplete(true);
        Intent myIntent = new Intent(this, MapActivity.class);
        // Todo: put additional input parameters
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (adapterView.equals(findViewById(R.id.spinner_environment))) {
            Singleton.getInstance().setEnvironmentImportance(i);
        } else if (adapterView.equals(findViewById(R.id.spinner_elevation))) {
            Singleton.getInstance().setElevationImportance(i - 1);
        } else if (adapterView.equals(findViewById(R.id.spinner_view))) {
            Singleton.getInstance().setViewImportance(i);
        } else if (adapterView.equals(findViewById(R.id.spinner_activity))) {
            Singleton.getInstance().setTypeOfActivity(i);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        /* do nothing */
    }
}
