package com.teamunemployment.breadcrumbs.Album;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.teamunemployment.breadcrumbs.R;

/**
 * @author Josiah Kendall
 */
public class AlbumView extends AppCompatActivity implements AlbumPresenterViewContract{


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_viewer);

        AlbumPresenter albumPresenter = new AlbumPresenter();
    }

    @Override
    public void setImageVisibility(int visible) {

    }

    @Override
    public void setVideoVisibility(int invisible) {

    }

    @Override
    public void setImageUrl(String id) {

    }
}
