package com.awoisoak.exposure.presentation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;

//TODO the random problems with the values might come for not using Big Decimals...should we try to use float instead of Double?
//TODO apply blue instead pink
//TODO create a "class converter" to apply a custom value (like 2.8) to a specific porcentage
// (like 10% of the seekbar)
//TODO add custom icon to the seekbar index so we could even get rid of the images at the left
//TODO let the user personalize the values?

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.tv_aperture)
    TextView tvAperture;
    @BindView(R.id.seekBar_aperture)
    SeekBar seekBarAperture;

    @BindView(R.id.tv_speed)
    TextView tvSpeed;
    @BindView(R.id.seekBar_shutter)
    SeekBar seekBarSpeed;

    @BindView(R.id.tv_iso)
    TextView tvISO;
    @BindView(R.id.seekBar_iso)
    SeekBar seekBarISO;

    @BindView(R.id.tv_nd_aperture)
    TextView tvApertureND;
    @BindView(R.id.seekBar_nd_aperture)
    SeekBar seekBarApertureND;

    @BindView(R.id.tv_nd_iso)
    TextView tvISOND;
    @BindView(R.id.seekBar_nd_iso)
    SeekBar seekBarISOND;

    @BindView(R.id.tv_nd_stops)
    TextView tvStopsND;
    @BindView(R.id.seekBar_nd_stops)
    SeekBar seekBarStopsND;

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

        switch (seekBar.getId()) {
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
                Log.e(TAG, "onProgressChanged | Seekbar not found");
        }
        calculateEV();
        calculateEVWithISO();
        calculateNDShutterSpeed();
    }


    /**
     * https://en.wikipedia.org/wiki/Exposure_value
     * Exposure value is a base-2 logarithmic scale defined by:
     *
     * EV = log₂ (N^2 / t)
     *
     * where:
     * - N is the relative aperture (f-number)
     * - t is the exposure time ("shutter speed") in seconds[2]
     *
     * Aperture f-1 AND shutter 1s gives you EV = 0.0
     */
    private void calculateEV() {
        /**
         * Seems like the Seekbars are triggering the events right away they are created.
         * We need to wait for the rest of seekbars to bee created too
         */
        if (tvSpeed.getText().equals("TextView")) {
            Log.e(TAG, "tvSpeed text is TextView...");
            return;
        }
        Double N = Double.parseDouble((String) tvAperture.getText());
        Double t = parseSpeed();
        Double EV = Math.log(N * N / t) / Math.log(2);
        Log.d(TAG, "EV = " + EV);
    }



    /**
     * https://photo.stackexchange.com/questions/32359/why-does-ev-increase-as-iso-increases
     *
     * EV = log₂(N²) + log₂(1/t) - log₂(100/S)
     * EV = aperture + shutter - ISO
     */
    private Double calculateEVWithISO() {
        /**
         * Seems like the Seekbars are triggering the events right away they are created.
         * We need to wait for the rest of seekbars to bee created too
         */
        if (tvSpeed.getText().equals("TextView") || tvISO.getText().equals("TextView")
                || tvISOND.getText().equals("TextView")) {
            Log.e(TAG, "tvSpeed text is TextView...");
            return -1.0;
        }
        Double N = Double.parseDouble((String) tvAperture.getText());
        Double t = parseSpeed();
        Double ISO = Double.parseDouble(((String) tvISO.getText()));
//        Double ISO_ND = Double.parseDouble(((String) tvISOND.getText()));
        Double EV = (Math.log(N * N) / Math.log(2)) +
                (Math.log(1 / t) / Math.log(2)) -
                (Math.log(100 / ISO) / Math.log(2));
        Log.d(TAG, "EV WITH ISO= " + EV);
        return EV;

    }

    /**
     * https://photo.stackexchange.com/questions/32359/why-does-ev-increase-as-iso-increases
     *
     * EV = log₂(N²) + log₂(1/t) - log₂(100/S)
     * EV = aperture + shutter - ISO
     *
     * where:
     * - N is the relative aperture (f-number)
     * - t is the exposure time ("shutter speed") in seconds[2]
     * - 100 is the default ISO
     * - S is the new ISO
     *
     * Isolating t:
     * t = 1/(2^EV + log₂(100/S) - log₂(N²))
     */
    private void calculateNDShutterSpeed() {
        /**
         * Seems like the Seekbars are triggering the events right away they are created.
         * We need to wait for the rest of seekbars to bee created too
         */
        if (tvSpeed.getText().equals("TextView") || tvISO.getText().equals("TextView")
                || tvISOND.getText().equals("TextView")) {
            Log.e(TAG, "tvSpeed text is TextView...");
            return;
        }
        Double apertureND = Double.parseDouble((String) tvApertureND.getText());
//        Double t = Double.parseDouble(((String) tvSpeed.getText()).split("s")[0]);
//        Double ISO = Double.parseDouble(((String) tvISO.getText()));
        Double ISO_ND = Double.parseDouble(((String) tvISOND.getText()));
        Double EV = calculateEVWithISO();

        Double shutterSpeed = 1 / (Math.pow(2, EV) +
                (Math.log(100 / ISO_ND) / Math.log(2)) -
                (Math.log(apertureND * apertureND) / Math.log(2)));


        Log.d(TAG, "New Shutter speed is= " + shutterSpeed);

    }

    /**
     * Convert the speed values into 'real' Double numbers
     * @return
     */
    private Double parseSpeed() {
        String tmp = ((String) tvSpeed.getText()).split("s")[0];
        if (tmp.contains("1/")) {
            tmp = tmp.split("1/")[1];
            return 1 / Double.parseDouble(tmp);
        }
        return Double.parseDouble(tmp);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
