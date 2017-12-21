package com.awoisoak.exposure.presentation;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;

//TODO there are cameras with electronic shutters that reach til 1/32000s (there are some) add
// them to the string array?
//TODO make functions for all the parsing/splits in the code?
//TODO Same for the ISO, cameras like sony a7s have higher values
//TODO add a method to remove X.0 values
//TODO apply blue instead pink
//TODO when everything work try to use float instead of double
//TODO add a ViewModel and implement calls to onsaveinstance...?
//TODO disable landscape mode
public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final float MAX_SPEED = 1f / 80000f;


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

    @BindView(R.id.tv_EV)
    TextView tv_EV;
    @BindView(R.id.tv_final_shutter_speed)
    TextView tv_final_sutther_speed;

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
                tvAperture.setText("f/" + apertureValues[i]);
                break;
            case R.id.seekBar_nd_aperture:
                tvApertureND.setText("f/" + apertureValues[i]);
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

        calculateFinalSpeed();
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
    private float calculateEVWithISO() {

        float N = Float.parseFloat(((String) tvAperture.getText()).split("f/")[1]);
        float t = parseSpeed((String) tvSpeed.getText());
        float ISO = Float.parseFloat(((String) tvISO.getText()));
        float EV = (float) ((Math.log(N * N) / Math.log(2)) + (Math.log(1 / t) / Math.log(2)) -
                        (Math.log(ISO / 100) / Math.log(2)));
        //We only want to display 1 digit
        String tmp = String.format("EV = %.1f", EV);
        tv_EV.setText(tmp);
        return EV;
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
    private void calculateFinalSpeed() {
        if (!areSeekBarInitialized()) {
            return;
        }
        Float apertureND = Float.parseFloat(((String) tvApertureND.getText()).split("f/")[1]);
        Float ISO_ND = Float.parseFloat(((String) tvISOND.getText()));
        Float EV = calculateEVWithISO();
        System.out.println("__________________________");
        Log.d(TAG, "EV = " + EV);
        Log.d(TAG, "apertureND = " + apertureND);
        Log.d(TAG, "ISO_ND = " + ISO_ND);

        float shutterSpeed = (float) ((100 * Math.pow(apertureND, 2)) / (ISO_ND * Math.pow(2, EV)));
        Log.d(TAG, "speed =  " + shutterSpeed);
        shutterSpeed = calculateSpeedWithNDFilterAdded(shutterSpeed,
                (Float.valueOf(((String) tvStopsND.getText()).split("-")[0])));
        tv_final_sutther_speed.setText(formatSpeed(shutterSpeed));
        Log.d(TAG, "speed ND=  " + shutterSpeed);
        System.out.println("__________________________");
    }

    /**
     * Given an original speed and the Stop value of the attached NF filter, it calculates the final
     * exposure time.
     * http://www.vassilistangoulis.com/gr/?p=4958
     *
     * Tnd = T0 * 2^ND
     *
     * where:
     * - ND is the Stop value of your ND filter
     * - T0 is the Base shutter speed (without filter attached) in seconds
     * - Tnd is the final exposure time
     */
    private float calculateSpeedWithNDFilterAdded(float originalSpeed, float stopValue) {
        return (float) (originalSpeed * Math.pow(2, stopValue));
    }

    /**
     * Format the final shutter speed to display to the user properly
     */
    private String formatSpeed(float unformattedSpeed) {
        int hours = (int) (unformattedSpeed / 3600);
        int minutes = (int) (unformattedSpeed % 3600) / 60;
        float seconds = unformattedSpeed % 60;

        String minutesToDisplay = String.valueOf(minutes);
        String hoursToDisplay = String.valueOf(hours);

        //Remove the X.0 values
        float fraction;
        fraction = minutes % 1;
        if (fraction == 0.0) {
            minutesToDisplay = minutesToDisplay.split("\\.")[0];
        }
        fraction = hours % 1;
        if (fraction == 0.0) {
            hoursToDisplay = hoursToDisplay.split("\\.")[0];
        }




        /*
        Seconds formatting are more tricky as per speed under 1s we will need all possible digits
         */
        String secondsToDisplay;
        if (hours >= 1 || minutes >= 1 || seconds > 30) {//Longer shutter speed than 30s
            seconds = (float) (StrictMath.round(seconds * 10.0) / 10.0);
            secondsToDisplay = String.valueOf(seconds) + "s";

            //Remove the X.0 values
            fraction = seconds % 1;
            if (fraction == 0.0) {
                secondsToDisplay = secondsToDisplay.split("\\.")[0] + "s";
            }
        } else { //Less than 30s

            float min = 30;
            int index = -1;
            float diff;
            for (int i = speedValues.length - 1; i >= 0; i--) {
                diff = Math.abs(seconds - parseSpeed(speedValues[i]));
                if (diff < min) {
                    min = diff;
                    index = i;
                } else {
                    break;
                }
            }


            //TODO we can not accept values bigger than 8000 cause is the maximum shutter speed
            //TODO it should be activated with f/22,1/20s,100 but is not (seems a problem
            // comparing hugh double values)
            //TODO once we pass it to float it might work?

            if (seconds < MAX_SPEED) {
                tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.red));
                //TODO add some text explaining the situation to the user
            } else {
                //TODO there is some bug when the final speed is even higher than 1s but still
                // appears read
                tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.black));
            }

            secondsToDisplay = String.valueOf((speedValues[index]));
        }


        String speed;
        if (minutes < 1 && hours < 1) {
            speed = secondsToDisplay;
        } else if (minutes >= 1 && hours <= 0) {
            if (minutes < 10) {
                speed = minutesToDisplay + "m " + secondsToDisplay;
            } else {
                speed = minutesToDisplay + "m " + secondsToDisplay;
            }
        } else {
            speed = hoursToDisplay + "h " + minutesToDisplay + "m " + secondsToDisplay;
        }

        Log.d(TAG, "formatted speed = " + speed);
        return speed;
    }

    /**
     * Seems like the Seekbars are triggering the events right away they are created.
     * We need to wait for the rest of seekbars to bee created too
     */
    //TODO find out a better way to hack this
    private boolean areSeekBarInitialized() {
        if (tvSpeed.getText().equals("TextView") || tvISO.getText().equals("TextView")
                || tvISOND.getText().equals("TextView") || tvStopsND.getText().equals("TextView")) {
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
    private float parseSpeed(String uSpeed) {
        String tmp = uSpeed.split("s")[0];
        if (tmp.contains("1/")) {
            tmp = tmp.split("1/")[1];
            return 1 / Float.parseFloat(tmp);
        }
        return Float.parseFloat(tmp);
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
