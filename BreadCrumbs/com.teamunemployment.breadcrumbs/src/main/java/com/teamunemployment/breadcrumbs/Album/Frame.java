package com.teamunemployment.breadcrumbs.Album;

/**
 * @author Josiah Kendall.
 */
public class Frame {

    public interface FrameContract {
        int getMediaType();
        void loadMedia(FrameLoadedCallback loadedCallback);
        String cancelMediaLoading();
        boolean isLoaded();

    }

    public interface FrameLoadedCallback {
        void onLoaded();
    }





}
