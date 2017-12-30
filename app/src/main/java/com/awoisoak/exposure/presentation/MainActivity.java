package com.awoisoak.exposure.presentation;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO make functions for all the parsing/splits in the code?
//TODO apply blue instead pink
//TODO add a ViewModel and implement calls to onsaveinstance...?
public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final float MAX_SPEED = 1f / 8000f;
    private static final float MAX_SPEED_ALLOWED_GAP = 0.00002125f;


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

    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.button)
    Button button;

    @BindView(R.id.tv_big_chronometer)
    TextView tv_big_chronometer;


    String[] apertureValues;
    String[] speedValues;
    String[] ISOValues;
    String[] StopsValues;
    private float finalShutterSpeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializeValues();
        initializeViews();
    }

    private void initializeValues() {
        apertureValues = getResources().getStringArray(R.array.apertures);
        speedValues = getResources().getStringArray(R.array.speeds);
        ISOValues = getResources().getStringArray(R.array.iso);
        StopsValues = getResources().getStringArray(R.array.stops);
    }

    private void initializeViews() {
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

        tv_big_chronometer.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
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

        finalShutterSpeed = (float) ((100 * Math.pow(apertureND, 2)) / (ISO_ND * Math.pow(2, EV)));
        Log.d(TAG, "speed =  " + finalShutterSpeed);
        finalShutterSpeed = calculateSpeedWithNDFilterAdded(finalShutterSpeed,
                (Float.valueOf(((String) tvStopsND.getText()).split("-")[0])));
        checkIfChronometerShouldBeDisplayed(finalShutterSpeed);
        tv_final_sutther_speed.setText(formatSpeed(finalShutterSpeed));
        Log.d(TAG, "speed ND=  " + finalShutterSpeed);
        System.out.println("__________________________");
    }

    /**
     * Display Chronometer (button if the final shutter speed is longer than 1.5s
     */
    private void checkIfChronometerShouldBeDisplayed(float finalShutterSpeed) {
        if (finalShutterSpeed > 1.5) {
            button.setVisibility(View.VISIBLE);
        } else {
            button.setVisibility(View.INVISIBLE);
        }
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
    String formatSpeed(float uSpeed) {

        checkMaxSpeed(uSpeed);

        int hours = (int) (uSpeed / 3600);
        int minutes = (int) (uSpeed % 3600) / 60;
        float seconds = uSpeed % 60;

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
        if (uSpeed == 0) {//TODO needed?
            secondsToDisplay = "0s";
        } else if (hours >= 1 || minutes >= 1 || seconds > 30) {//Longer shutter speed than 30s
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
     * We need to figure out when the final speed is lower than 1/8000 to set it as red
     * (cameras are normally not that fast)
     *
     * There are some tricky cases when the speed is just a bit more than 1/8000 by some decimals so
     * we consider that speed correct
     *
     * If the speed is too far from the 1/8000 value (meaning it's closer to an
     * impossible speed for the camera) we will set the text to red
     */
    private void checkMaxSpeed(float uSpeed) {
        if ((uSpeed < MAX_SPEED) && (MAX_SPEED - uSpeed > MAX_SPEED_ALLOWED_GAP)) {
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.red));
            //TODO add some text explaining the situation to the user
        } else {
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.black));
        }
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


    @OnClick(R.id.button)
    void submit() {
        String status = (String) button.getText();
        switch (status) {
            case "Start":
                button.setText("Stop");
                progressBar.setMax((int) finalShutterSpeed);
                progressBar.setProgress(0);
                tv_big_chronometer.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                new ChronometerAsyncTask(this).execute(finalShutterSpeed % 60);
                break;
            case "Stop":
                button.setText("Start");
                progressBar.setMax((int) finalShutterSpeed);
                progressBar.setProgress(0);
                tv_big_chronometer.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                new ChronometerAsyncTask(this).cancel(false);
                break;

            default:
                Log.e(TAG, "The button is in unknown status");
        }
    }


    /**
     * Method to access to the finalShutterSpeed from the Asynctask
     */
    public float getFinalShutterSpeed() {
        return Math.round(finalShutterSpeed);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
