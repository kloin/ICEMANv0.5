package com.teamunemployment.breadcrumbs;

import android.util.Log;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by jek40 on 9/05/2016.
 */
public class BreadcrumbsVideoProgressTimer {

    private int videoTimer = 0;
    private Timer t;
    private ProgressBar progressBar;
    private int duration = 0;
    private boolean isRunning = false;
    private ITimer iTimer;
    public BreadcrumbsVideoProgressTimer(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public interface ITimer {
        public void OnFinished();
    }

    public void startTimerWithCallback(ITimer callback) {
        this.iTimer = callback;
        startTimer();
    }

    public void startTimer() {
        if (isRunning) {
            // Think we should throw an exception here because it will break
            return;
        }

        t=new Timer();

        isRunning = true;
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                videoTimer += 50;
                if (videoTimer < duration) {
                    progressBar.setProgress(videoTimer);
                } else {
                    Log.d("TIMER", "Finished loop, starting next");
                    // Stop video, and go to the next page
                    videoTimer = 0;

                    // Do our callback
                    if (iTimer != null ) {
                        iTimer.OnFinished();
                    }

                    // Stop this timer propogating by cancelling it here. Not sure this is the best strategy so I will need to test this when Im not so tired.
                    t.cancel();

                }
            }
        }, 50, 50);
    }

    public int GetDisplayTimer() {
        return videoTimer;
    }

    // DO we want to do this or just set it back at 0? maybe rewind a second
    public void SetDisplayTimer(int displayTimer) {
        videoTimer = displayTimer;
    }

    public void SetTimerDuration(int duration) {
        this.duration = duration;
        progressBar.setMax(duration);
    }

    /*
        Stops the timer.
     */
    public void StopTimer() {
        isRunning = false;
        if (t != null) {
            t.cancel();
        }

        // Remove the callback
        iTimer = null;
    }

    public boolean isRunning() {
        return isRunning;
    }

    /*
        Sets the timer back to 0.
     */
    public void ResetTimer() {
        Log.d("TIMER", "Resetting progress and timer count to 0");
        progressBar.setProgress(0);
        videoTimer = 0;
    }

    /*
        Set the max for the progress bar (In milliseconds)
     */
    public void SetTimerMax(int max) {
        progressBar.setMax(max);
    }


}
