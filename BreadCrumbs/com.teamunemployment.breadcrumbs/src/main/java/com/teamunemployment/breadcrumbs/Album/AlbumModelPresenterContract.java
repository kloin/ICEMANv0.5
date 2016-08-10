package com.teamunemployment.breadcrumbs.Album;

import com.teamunemployment.breadcrumbs.Album.data.FrameDetails;

/**
 * @author Josiah Kendall.
 */
public interface AlbumModelPresenterContract {

    void setFrame(FrameDetails frameDetails);
    void setBuffering(int visibility);
}
