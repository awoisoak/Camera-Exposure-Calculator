package com.awoisoak.exposure.presentation;

import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.awoisoak.exposure.R;

import java.lang.ref.WeakReference;

/**
 * AsyncTask implementing the chronometer and managing the different views through WeakReferences to
 * avoid leaks
 */
public class ChronometerAsyncTask extends AsyncTask {
    private WeakReference<TextView> tv_final_shutter_speed;
    private WeakReference<ProgressBar> progressBar;
    private WeakReference<Button> button;
    private WeakReference<MainActivity> mActivity;
    private WeakReference<TextView> tv_big_chronometer;
    private static CountDownTimer countdown;
    private boolean countdownFinished;

    public ChronometerAsyncTask(MainActivity activity) {
        mActivity = new WeakReference<>(activity);
        progressBar = new WeakReference<>((ProgressBar) activity.findViewById(R.id.progressBar));
        tv_final_shutter_speed = new WeakReference<>(
                (TextView) activity.findViewById(R.id.tv_final_shutter_speed));
        button = new WeakReference<>((Button) activity.findViewById(R.id.button));
        tv_big_chronometer = new WeakReference<>(
                (TextView) activity.findViewById(R.id.tv_big_chronometer));
    }

    @Override
    protected Object doInBackground(final Object[] params) {
        countdown.start();
        return "Task Completed";
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (areWeakReferencesValid()) {
            countdown.cancel();
            setViewsEnabled(true);
            tv_final_shutter_speed.get().setText(
                    String.valueOf(mActivity.get().getFinalShutterSpeed()));
        }
    }

    @Override
    /**
     * Values[0] = progress bar value
     * Values[1] = time left
     */
    protected void onProgressUpdate(Object[] values) {
        super.onProgressUpdate(values);
        if (areWeakReferencesValid()) {
            int progress = (int) (long) values[0];
            int timeLeft = (int) values[1];
            tv_big_chronometer.get().setText(String.valueOf(timeLeft));
            progressBar.get().setProgress(progress);
        }
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        if (countdownFinished && areWeakReferencesValid()) {
            button.get().setText("Start");
            progressBar.get().setProgress(0);
            setViewsEnabled(true);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (areWeakReferencesValid()) {
            countdownFinished = false;
            setViewsEnabled(false);
            final long uSpeed = (long) mActivity.get().getFinalShutterSpeed();
            final Object[] params = new Object[2];
            countdown = new CountDownTimer(uSpeed * 1000, 1000) {
                long progress = 0;
                int timeLeft = (int) uSpeed;

                @Override
                public void onTick(long l) {
                    progress = progress + 1;
                    timeLeft = timeLeft - 1;
                    params[0] = progress;
                    params[1] = timeLeft;
                    publishProgress(params);
                }

                @Override
                public void onFinish() {
                    System.out.println("CountDownTimer | onFinish | Thread name=  "+Thread.currentThread().getName() +" id ="+Thread.currentThread().getId());
                    countdownFinished = true;
                    //When we call to onPostExecute manually, it has to run in the UI thread
                    mActivity.get().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onPostExecute(new Object());
                        }
                    });
                }
            };
        }
    }

    private void setViewsEnabled(boolean enable) {
        if (areWeakReferencesValid()) {

            ConstraintLayout layout = mActivity.get().findViewById(R.id.constraint_layout);
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                child.setEnabled(enable);
                if (enable) {
                    child.setAlpha(1f);
                } else {
                    child.setAlpha(0.05f);
                }
            }
            button.get().setEnabled(true);
            button.get().setAlpha(1f);
            progressBar.get().setEnabled(true);
            progressBar.get().setAlpha(1f);
            tv_big_chronometer.get().setEnabled(true);
            tv_big_chronometer.get().setAlpha(1f);

            progressBar.get().setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
            tv_big_chronometer.get().setVisibility(enable ? View.INVISIBLE : View.VISIBLE);
            tv_final_shutter_speed.get().setVisibility(enable ? View.VISIBLE : View.INVISIBLE);

        }
    }


    private boolean areWeakReferencesValid() {
        if (mActivity.get() != null && tv_final_shutter_speed.get() != null
                && progressBar.get() != null && button.get() != null
                && tv_big_chronometer.get() != null) {
            return true;
        } else {
            return false;
        }
    }
}
