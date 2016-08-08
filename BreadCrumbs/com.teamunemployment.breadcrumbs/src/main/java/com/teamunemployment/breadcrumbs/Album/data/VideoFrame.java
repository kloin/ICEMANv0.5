package com.teamunemployment.breadcrumbs.Album.data;

import com.teamunemployment.breadcrumbs.Album.Frame;

/**
 * @author Josiah Kendall.
 */
public class VideoFrame implements Frame.FrameContract {

    @Override
    public int getMediaType() {
        return 0;
    }

    @Override
    public void loadMedia(Frame.FrameLoadedCallback loadedCallback) {

    }

    @Override
    public String cancelMediaLoading() {
        return null;
    }

    @Override
    public boolean isLoaded() {
        return false;
    }
}
