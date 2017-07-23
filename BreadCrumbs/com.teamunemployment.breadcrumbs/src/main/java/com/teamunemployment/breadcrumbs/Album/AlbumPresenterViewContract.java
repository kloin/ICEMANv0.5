package com.teamunemployment.breadcrumbs.Album;

import android.graphics.Bitmap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Josiah Kendall.
 */
public interface AlbumPresenterViewContract {

    void setImageVisibility(int visible);
    void setVideoVisibility(int invisible);
    void setImageUrl(String id);
    void setImageBitmap(Bitmap bitmap);
    void setScreenMessage(String message, float posX, float posY);
    void setProfilePicture(String url);
    void showMessage(String message);
    void setBufferingVisible();
    void setBufferingInvisible();
    void setUserName(String userName);
    void setProgressBarState(int position);
    void setProgressBarMax(int max);
    void finishUp();
    void showCommentsBottomSheet();
    void showDimScreenOverlay();
    void hideCommentsBottomSheet();
    void hideDimScreenOverlay();
    void setCommentsCount(int size);
    void setRecyclerViewAdapter(CommentAdapter commentAdapter);
    void SetImageViewWithImage(String url, CircleImageView profileImage);
    void showNoContentMessage();
    void setImageViewCount(String countString);
    void setSettingsButton(int visible);
    void setPlayCountVisibility(int visibility);
}
