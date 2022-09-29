package com.awoisoak.exposure.presentation;


import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//TODO save button status with tags instead of texts (otherwise it won't work with other language)
//TODO Add Icons for small screens with good enough resolution phones like Moto G

/**
 * Only activity in the app to display controls and different values to the user
 */
public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {


    private static final String TAG = MainActivity.class.getSimpleName();

    private static final float MAX_SPEED = 1f / 8000f;
    private static final float MAX_SPEED_ALLOWED_GAP = 0.00002125f;
    private static final float MAX_EXPOSURE_VALUE = 7 * 24 * 60 * 60;
    private static final float MIN_CHRONOMETER_SPEED = 3.8f;
    private static final String THEME_ID = "theme_id";

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

    ActionBar actionbar;

    String[] apertureValues;
    String[] speedValues;
    String[] ISOValues;
    String[] StopsValues;
    private float finalShutterSpeed;
    private Snackbar snackbar;
    private int themeToApply;
     ChronometerAsyncTask asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            setTheme(savedInstanceState.getInt(THEME_ID, R.style.DarkExposureTheme));
        }
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
        actionbar = getSupportActionBar();
        actionbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00BCD4")));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        if (asyncTask == null || asyncTask.isCountdownFinished()) {
            getMenuInflater().inflate(R.menu.main, menu);

        } else {
            for (int i = 0; i < menu.size(); i++) {
                menu.getItem(i).setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.night_mode_button) {
            TypedValue outValue = new TypedValue();
            getTheme().resolveAttribute(R.attr.themeName, outValue, true);
            if ("AppTheme".equals(outValue.string)) {
                themeToApply = R.style.DarkExposureTheme;
            } else if ("DarkExposureTheme".equals(outValue.string)) {
                themeToApply = R.style.AppTheme;
            }
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(THEME_ID, themeToApply);
        super.onSaveInstanceState(outState);
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
        finalShutterSpeed = (float) ((100 * Math.pow(apertureND, 2)) / (ISO_ND * Math.pow(2, EV)));
        finalShutterSpeed = calculateSpeedWithNDFilterAdded(finalShutterSpeed,
                (Float.valueOf(((String) tvStopsND.getText()).split("-")[0])));
        tv_final_sutther_speed.setText(formatSpeed(finalShutterSpeed));
        Log.d(TAG, "speed ND=  " + finalShutterSpeed);
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
        return (float) Math.abs(originalSpeed * Math.pow(2, stopValue));
    }

    /**
     * Format the final shutter speed to display to the user properly
     */
    String formatSpeed(float uSpeed) {
        checkThresholds(uSpeed);
        boolean tooFastToBeDisplayed = false;

        int hours = (int) (uSpeed / 3600);
        int minutes = (int) (uSpeed % 3600) / 60;
        float seconds = uSpeed % 60;

        if (uSpeed / 3600 > Integer.MAX_VALUE) {
            tooFastToBeDisplayed = true;
        }
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
            secondsToDisplay = String.valueOf((speedValues[index]));
        }


        String speed;

        if (tooFastToBeDisplayed) {
            speed = "Excessive exposure";
        } else if (minutes < 1 && hours < 1) {
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
     * Method to calculate whether the shutter speed is too fast or too slow:
     *
     * - If the final speed is lower than 1/8000 to set it as red (cameras are normally not that
     * fast). There are some tricky cases when the speed is just a bit more than 1/8000 by some
     * decimals so  we consider that speed correct.If the speed is too far from the 1/8000 value
     * (meaning it's closer to an impossible speed for the camera) we will set the text to red and
     * display a snackbar explaining the problem to the user
     *
     * - On the other hand, if the shutter speed is too slow (super long exposure value) many
     * cameras can not support it
     * I couldn't find the specific max bulb mode time for many popular cameras so I will just
     * setup
     * a random max value (long enough for any 'normal' use)
     *
     * Besides that, we will hide the Chronometer button when the final speed is lower than
     * {MIN_CHRONOMETER_SPEED} as
     * it's kind of useless
     */
    private void checkThresholds(float uSpeed) {
        if ((uSpeed < MAX_SPEED) && (MAX_SPEED - uSpeed > MAX_SPEED_ALLOWED_GAP)) {
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.red));
            displaySnackbar(R.string.maximum_shutter_speed_explanation);
        } else if ((uSpeed > MAX_EXPOSURE_VALUE)) {
            button.setVisibility(View.INVISIBLE);
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.red));
            displaySnackbar(R.string.maximum_exposure_time_explanation);
        } else if (uSpeed < MIN_CHRONOMETER_SPEED) {
            button.setVisibility(View.INVISIBLE);
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            hideSnackbar();
        } else {
            button.setVisibility(View.VISIBLE);
            tv_final_sutther_speed.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            hideSnackbar();
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
                if (asyncTask== null) {
                    button.setText("Stop");
                    //multiply by 100 for the animation to be smoothie
                    progressBar.setMax(Math.round(finalShutterSpeed) * 100);
                    progressBar.setProgress(0);
                    ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0,
                            progressBar.getMax());
                    long animationDuration = (long) Math.abs(Math.round(finalShutterSpeed) * 1000);
                    System.out.println("awoo animationDuration=" + animationDuration);
                    animation.setDuration(animationDuration);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.start();
                    tv_big_chronometer.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    asyncTask = (ChronometerAsyncTask) new ChronometerAsyncTask(this).execute(
                            finalShutterSpeed % 60);
                }
                break;
            case "Stop":
                asyncTask.cancel(false);
                button.setText("Start");
                progressBar.setMax((int) finalShutterSpeed);
                progressBar.setProgress(0);
                tv_big_chronometer.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                break;

            default:
                Log.e(TAG, "The button is in unknown status");
        }
    }


    /**
     * Method to access to the finalShutterSpeed from the AsyncTask
     *
     * @param rounded if true, the value will be automatically rounded
     */
    public float getFinalShutterSpeed(boolean rounded) {
        return rounded ? Math.round(finalShutterSpeed) : finalShutterSpeed;

    }


    /**
     * Display Snackbar (if there is not one in the screen already) with the passed message
     */
    private void displaySnackbar(int messageId) {
        if (snackbar == null || !snackbar.isShown()) {

            /**
             Hack to change the text color in a support snackbar
             https://stackoverflow.com/questions/31061474/how-to-set-support-library-snackbar-text
             -color-to-something-other-than-android
             **/
            String snackText = getResources().getString(messageId);
            SpannableStringBuilder ssb = new SpannableStringBuilder()
                    .append(snackText);
            ssb.setSpan(new ForegroundColorSpan(Color.WHITE), 0, snackText.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            snackbar = Snackbar.make(findViewById(R.id.constraint_layout), ssb,
                    Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }
    }

    /**
     * Hide snackbar if it's being displayed
     */
    private void hideSnackbar() {
        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
    }


    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
