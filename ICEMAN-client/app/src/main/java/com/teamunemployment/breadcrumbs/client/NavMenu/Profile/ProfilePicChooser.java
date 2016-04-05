package com.teamunemployment.breadcrumbs.client.NavMenu.Profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;

import java.io.IOException;

/**
 * Created by Josiah kendall on 4/23/2015.
 */
public class ProfilePicChooser extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_screen);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = Uri.parse(data.getData().toString());
        try {
            //Set our profile picture, then save it
            Bitmap media = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ImageView profilePic = (ImageView) findViewById(R.id.profilePicture);
            profilePic.setImageBitmap(media);
            String userId = GlobalContainer.GetContainerInstance().GetUserId();
            userId = userId + "P";
            // Save our profile picture
            AsyncImageFetch imagesave = new AsyncImageFetch(media, userId, new AsyncImageFetch.RequestListener() {

                /*
                 * Override for the onFinished.
                 */
                @Override
                public void onFinished(String result) {

                    //finish();
                }
            });

            imagesave.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
