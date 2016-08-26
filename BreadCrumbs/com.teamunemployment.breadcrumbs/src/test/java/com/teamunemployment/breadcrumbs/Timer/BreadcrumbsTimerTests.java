package com.teamunemployment.breadcrumbs.Timer;

import com.teamunemployment.breadcrumbs.BreadcrumbsTimer;
import com.teamunemployment.breadcrumbs.Location.SimpleGps;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


/**
 * @author Josiah
 *
 * Tests for the timer class.
 */
public class BreadcrumbsTimerTests {

    @Test
    public void EnsureWeCanStartTimerWithoutProgressBar() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //112
        verify(timerCompleteListener, times(1)).onCompleted();
    }

    @Test
    public void EnsureThatTimerStopsWhenWeReachEndOfDuration() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96

        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //112
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
    }

    @Test
    public void EnsureWeCanStopTimer() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96

        timer.Stop();
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
    }

    @Test
    public void EnsureWeCanRestartTimer() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.Stop();
        timer.RestartTimer();
        timer.setTimerMax(100);
        timer.setDuration(100);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //32
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //48
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //64
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //80
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //96
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //112
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_STOPPED);
    }

    @Test
    public void EnsureOnCompletionListenerIsCalled() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //112
        verify(timerCompleteListener, times(1)).onCompleted();
    }

    @Test
    public void EnsureWeCanPauseAndResumeTimer() {
        int duration = 100;
        BreadcrumbsTimer.TimerCompleteListener timerCompleteListener = Mockito.mock(BreadcrumbsTimer.TimerCompleteListener.class);
        BreadcrumbsTimer timer = new BreadcrumbsTimer(duration,timerCompleteListener);
        timer.setTimerState(BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //16
        timer.runTimerLoop();   //32
        timer.runTimerLoop();   //48
        timer.runTimerLoop();   //64
        timer.runTimerLoop();   //80
        timer.runTimerLoop();   //96
        timer.Pause();
        timer.runTimerLoop();   //96
        timer.runTimerLoop();   //96
        timer.runTimerLoop();   //96
        timer.runTimerLoop();   //96
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_PAUSED);
        timer.Resume();
        Assert.assertTrue(timer.getTimerState() == BreadcrumbsTimer.STATE_RUNNING);
        timer.runTimerLoop();   //112
        verify(timerCompleteListener, times(1)).onCompleted();
    }
}
