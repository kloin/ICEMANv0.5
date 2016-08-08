package com.teamunemployment.breadcrumbs.Album.data;

import com.teamunemployment.breadcrumbs.Album.Frame;

import java.io.File;

/**
 * Created by jek40 on 3/08/2016.
 */
public class WebPFrame implements Frame.FrameContract{
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

    public File GetMedia() {
        return null;
    }
}
