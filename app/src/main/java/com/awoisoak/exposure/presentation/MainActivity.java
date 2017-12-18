package com.awoisoak.exposure.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;

//TODO apply blue instead pink
//TODO create a "class converter" to apply a custom value (like 2.8) to a specific porcentage
// (like 10% of the seekbar)
//TODO add custom icon to the seekbar index so we could even get rid of the images at the left
//TODO let the user personalize the values?

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tv_aperture) TextView tvAperture;
    @BindView(R.id.seekBar_aperture) SeekBar seekBarAperture;

    @BindView(R.id.tv_speed) TextView tvSpeed;
    @BindView(R.id.seekBar_shutter) SeekBar seekBarSpeed;

    @BindView(R.id.tv_iso) TextView tvISO;
    @BindView(R.id.seekBar_iso) SeekBar seekBarISO;

    @BindView(R.id.tv_nd_aperture) TextView tvApertureND;
    @BindView(R.id.seekBar_nd_aperture) SeekBar seekBarApertureND;

    @BindView(R.id.tv_nd_iso) TextView tvISOND;
    @BindView(R.id.seekBar_nd_iso) SeekBar seekBarISOND;

    @BindView(R.id.tv_nd_stops) TextView tvStopsND;
    @BindView(R.id.seekBar_nd_stops) SeekBar seekBarStopsND;

    String[] apertureValues;
    String[] speedValues;
    String[] ISOValues;
    String[] StopsValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializeValues();
        initializeSeekBars();
    }

    private void initializeValues() {
        apertureValues = getResources().getStringArray(R.array.apertures);
        speedValues = getResources().getStringArray(R.array.speeds);
        ISOValues = getResources().getStringArray(R.array.iso);
        StopsValues = getResources().getStringArray(R.array.stops);
    }

    private void initializeSeekBars() {
        seekBarAperture.setOnSeekBarChangeListener(this);
        seekBarApertureND.setOnSeekBarChangeListener(this);
        seekBarSpeed.setOnSeekBarChangeListener(this);
        seekBarISO.setOnSeekBarChangeListener(this);
        seekBarISOND.setOnSeekBarChangeListener(this);
        seekBarStopsND.setOnSeekBarChangeListener(this);

        seekBarAperture.setMax(apertureValues.length - 1);
        seekBarApertureND.setMax(apertureValues.length - 1);
        seekBarSpeed.setMax(speedValues.length - 1);
        seekBarISO.setMax(ISOValues.length - 1);
        seekBarISOND.setMax(ISOValues.length - 1);
        seekBarStopsND.setMax(StopsValues.length - 1);
    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

        switch(seekBar.getId()){
            case R.id.seekBar_aperture:
                tvAperture.setText(apertureValues[i]);
                break;
            case R.id.seekBar_nd_aperture:
                tvApertureND.setText(apertureValues[i]);
                break;
            case R.id.seekBar_shutter:
                tvSpeed.setText(speedValues[i]);
                break;
            case R.id.seekBar_iso:
                tvISO.setText(ISOValues[i]);
                break;
            case R.id.seekBar_nd_iso:
                tvISOND.setText(ISOValues[i]);
                break;
            case R.id.seekBar_nd_stops:
                tvStopsND.setText(StopsValues[i]);
                break;
            default:
                Log.e(TAG,"onProgressChanged | Seekbar not found");
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
