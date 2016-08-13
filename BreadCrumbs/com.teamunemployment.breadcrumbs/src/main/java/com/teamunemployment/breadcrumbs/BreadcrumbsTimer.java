package com.teamunemployment.breadcrumbs;

import android.util.Log;
import android.widget.ProgressBar;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Josiah Kendall
 */
public class BreadcrumbsTimer {

    // Timer states.
    public static final int STATE_PAUSED = 2;
    public static final int STATE_RUNNING = 1;
    public static final int STATE_STOPPED = 0;

    public void Resume() {
        state = STATE_RUNNING;
    }



    public interface TimerCompleteListener {
        void onCompleted();
    }

    private int state = 0;
    private int duration;
    private TimerCompleteListener onFinishedListener;
    private ProgressBar progressBar;
    private Timer t;
    private int currentTime = 0;

    public BreadcrumbsTimer(int duration, TimerCompleteListener timerCompleteListener) {
        this.duration = duration;
        this.onFinishedListener = timerCompleteListener;
    }

    /**
     * Constructor. Enables setting a progress bar which is updated by the timer.
     * @param duration Our timeing duration.
     * @param timerCompleteListener The completion callback.
     * @param progressBar The progressbar to update.
     */
    public BreadcrumbsTimer(int duration, TimerCompleteListener timerCompleteListener, ProgressBar progressBar) {
        this.duration = duration;
        this.onFinishedListener = timerCompleteListener;
        this.progressBar = progressBar;
    }

    /**
     * Set the duration of the timer.
     * @param max The length of the progressbar in milliseconds
     */
    public void setTimerMax(int max) {
        if (progressBar != null) {
            progressBar.setMax(max);
        }

    }

    /**
     * Stops the timer, and sets it to zero. Any onfinished callbacks are not triggered.
     */
    public void RestartTimer() {
        this.duration = 0;
        if (t != null) {
            t.cancel();
            t = null;
        }
    }

    /**
     * Stop the timer. This effectivly destroys your timer. It needs to be rebuilt after calling this function.
     */
    public void Stop() {
        if (t != null) {
            t.cancel();
            t = null;
        }

        state = STATE_STOPPED;
    }

    /**
     * Set the timers current state. only really here for testing.
     * @param state
     */
    public void setTimerState(int state) {
        this.state = state;
    }
    /**
     * Start the timer.
     */
    public void Start() {
        if (state != STATE_PAUSED) {
            runTimer();
        }

        state = STATE_RUNNING;
    }

    /**
     * Pause the timer.
     */
    public void Pause() {
        state = STATE_PAUSED;
    }

    /**
     * Get the current state of the timer.
     * @return the timer state. 0 = stopped, 1 = running, 2 = paused.
     */
    public int getTimerState() {
        return state;
    }

    /**
     * Run the timer.
     */
    private void runTimer() {
        if (t != null) {
            throw new IllegalStateException("Timer dirty. Must call restart() to reuse a timer.");
        }
        t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runTimerLoop();
            }
        }, 16, 16);
    }

    /**
     * Get the current position of the timer
     * @return
     */
    public int getTimerPosition() {
        return currentTime;
    }

    /**
     * Public so that we can test it.
     */
    public void runTimerLoop() {
        if (state == STATE_RUNNING) {
            currentTime += 16;
            if (currentTime < duration) {
                if (progressBar != null) {
                    progressBar.setProgress(currentTime);

                }
            } else {
                // Stop video, and go to the next page
                currentTime = 0;
                // Do our callback
                if (onFinishedListener != null ) {
                    onFinishedListener.onCompleted();
                    state = STATE_STOPPED;
                }

                // Stop this timer propogating by cancelling it here. Not sure this is the best strategy so I will need to test this when Im not so tired.
                if (t != null) {
                    t.cancel();
                }
            }
        }
    }

}
