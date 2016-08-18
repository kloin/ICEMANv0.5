package com.teamunemployment.breadcrumbs.MediaPlayer;

import android.media.MediaPlayer;

import com.teamunemployment.breadcrumbs.MediaPlayerWrapper;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.asm.util.CheckClassAdapter.verify;

/**
 * Created by jek40 on 13/08/2016.
 */
public class MediaplayerTests {

    @Test
    public void TestWeSeekToCorrectPositionWhenResuming() {
        MediaPlayer mediaPlayer = Mockito.mock(MediaPlayer.class);
        MediaPlayerWrapper mediaPlayerWrapper = new MediaPlayerWrapper(mediaPlayer);
        mediaPlayerWrapper.SetTrack("mock");
        mediaPlayerWrapper.Prepare();
        mediaPlayerWrapper.Play();

        when(mediaPlayer.getCurrentPosition()).thenReturn(300);
        mediaPlayerWrapper.Stop();
        Mockito.verify(mediaPlayer, times(1)).getCurrentPosition();

        mediaPlayerWrapper.Play();
        Mockito.verify(mediaPlayer, times(1)).seekTo(300);




    }
}

