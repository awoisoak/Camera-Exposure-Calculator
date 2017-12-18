package com.awoisoak.exposure.presentation;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.spinner_aperture) Spinner spinnerAperture;
    @BindView(R.id.spinner_shutter_speed) Spinner spinnerSpeed;
    @BindView(R.id.spinner_iso) Spinner spinnerISO;
    @BindView(R.id.spinner_nd_aperture) Spinner spinnerNDAperture;
    @BindView(R.id.spinner_nd_iso) Spinner spinnerNDISO;
    @BindView(R.id.spinner_nd_stops) Spinner spinnerNDStops;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializeSpinners();
    }

    private void initializeSpinners() {
        ArrayAdapter<CharSequence> apertureAdapter = ArrayAdapter.createFromResource(this,
                R.array.apertures, R.layout.support_simple_spinner_dropdown_item);
        apertureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerAperture.setAdapter(apertureAdapter);
        spinnerNDAperture.setAdapter(apertureAdapter);

        ArrayAdapter<CharSequence> isoAdapter = ArrayAdapter.createFromResource(this,
                R.array.iso, R.layout.support_simple_spinner_dropdown_item);
        isoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerISO.setAdapter(isoAdapter);
        spinnerNDISO.setAdapter(isoAdapter);

        ArrayAdapter<CharSequence> speedAdapter = ArrayAdapter.createFromResource(this,
                R.array.speeds, R.layout.support_simple_spinner_dropdown_item);
        speedAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerSpeed.setAdapter(speedAdapter);

        ArrayAdapter<CharSequence> stopsAdapter = ArrayAdapter.createFromResource(this,
                R.array.stops, R.layout.support_simple_spinner_dropdown_item);
        stopsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinnerNDStops.setAdapter(stopsAdapter);
    }

    @OnItemSelected({R.id.spinner_aperture, R.id.spinner_nd_aperture, R.id.spinner_iso,
            R.id.spinner_nd_iso, R.id.spinner_shutter_speed, R.id.spinner_nd_stops})
    public void whatever(AdapterView<?> parent, View view,
            int pos, long id) {
        Toast.makeText(this, "id selected = "+id, Toast.LENGTH_SHORT).show();
    }


}
