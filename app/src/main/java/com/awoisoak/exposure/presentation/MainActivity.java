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
//TODO display the EV in a textview

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
     * the  log₂(100/S) is wrong on that link!
     *
     * EV = log₂(N²) + log₂(1/t) - log₂(S/100)
     * EV = aperture + shutter - ISO
     *
     *
     * https://photo.stackexchange.com/questions/73304/when-to-use-the-lv-formula
     * Another way to look at it
     * EV = log2(f^2/T)          Exposure Value
     * LV = EV + log2(ISO/100)   Light Value (= EV assumes ISO 100)
     */
    private Double calculateEVWithISO() {

        Double N = Double.parseDouble((String) tvAperture.getText());
        Double t = parseSpeed();
        Double ISO = Double.parseDouble(((String) tvISO.getText()));
        return (Math.log(N * N) / Math.log(2)) + (Math.log(1 / t) / Math.log(2)) -
                (Math.log(ISO / 100) / Math.log(2));
    }

    /**
     * https://photo.stackexchange.com/questions/32359/why-does-ev-increase-as-iso-increases
     * the  log₂(100/S) is wrong on that link!
     * The correct formula appears in Wikipedia
     * https://en.wikipedia.org/wiki/Exposure_value
     *
     * EV = log₂(N²) + log₂(1/t) - log₂(S/100)
     * EV = aperture + shutter - ISO
     *
     * t = S*N²/100*2^EV
     *
     * * where:
     * - N is the relative aperture (f-number)
     * - t is the exposure time ("shutter speed") in seconds[2]
     * - 100 is the default ISO
     * - S is the new ISO
     */
    private void calculateNDShutterSpeed() {
        if (!areSeekBarInitialized()) {
            return;
        }
        Double apertureND = Double.parseDouble((String) tvApertureND.getText());
        Double ISO_ND = Double.parseDouble(((String) tvISOND.getText()));
        Double EV = calculateEVWithISO();
        System.out.println("__________________________");
        Log.d(TAG, "EV = " + EV);
        Log.d(TAG, "apertureND = " + apertureND);
        Log.d(TAG, "ISO_ND = " + ISO_ND);

        Double shutterSpeed = (100 * Math.pow(apertureND, 2)) / (ISO_ND * Math.pow(2, EV));

        Log.d(TAG, "speed =  " + shutterSpeed);
        System.out.println("__________________________");
    }

    /**
     * Seems like the Seekbars are triggering the events right away they are created.
     * We need to wait for the rest of seekbars to bee created too
     */
    //TODO find out a better way to hack this
    private boolean areSeekBarInitialized() {
        if (tvSpeed.getText().equals("TextView") || tvISO.getText().equals("TextView")
                || tvISOND.getText().equals("TextView")) {
            Log.e(TAG, "tvSpeed text is TextView...");
            return false;
        }
        return true;
    }


//    //TODO try this formula?
//    https://photo.stackexchange.com/questions/89563/is-there-a-formula-to-calculate-iso
// -according-to-shutter-speed
//    Mathematically, you can calculate it all via:
//
//    sISO = ln(ISO / 100) / ln(2)
//    sAperture = -ln(Aperture) / ln(√2)
//    sShutter = EV + sISO + sAperture
//    Shutter speed = 2-sShutter

    /**
     * Convert the speed values into 'real' Double numbers
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
