package com.teamunemployment.breadcrumbs.client.tabs;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Facebook.AccountManager;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.GETImageSaver;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.client.Adapters.FacebookImageGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.Adapters.LocalFilesGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.ImageChooser.GridImageSelector;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by jek40 on 10/03/2016.
 */
public class LocalPhotosTab extends GridImageSelector {
    private TextView emptyGridInfo;
    @Override
    public void setUpGridAndListeners() {
        //String facebookId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("FACEBOOK_REGISTRATION_ID", "-1");
        //if (facebookId.equals("-1")) {
          //  return;
       // }
        emptyGridInfo = (TextView) rootView.findViewById(R.id.empty_grid_placeholder);
        loadLocalImages();
    }

    private void loadLocalImages() {
        final ArrayList<String> ids = new ArrayList<>();
        final GridView gridview = (GridView)  rootView.findViewById(R.id.gridView1);
        Cursor cc = activityContext.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
                null);
        for (int i = 0; i < cc.getCount(); i++) {
            cc.moveToPosition(i);
            ids.add(cc.getString(1));
        }
        LocalFilesGridViewAdapter adapter = new LocalFilesGridViewAdapter(ids, activityContext);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                try {
                    // Save our profile header pic.
                    String url = ids.get(position);
                    Uri uri = Uri.parse("file://"+url);
                    final String userId = PreferenceManager.getDefaultSharedPreferences(context).getString("USERID","-1");
                    final BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
                    thumbOpts.inSampleSize = 4;
                    Bitmap bm = BitmapFactory.decodeFile(url, thumbOpts);
                    ExifInterface exif = null;
                    exif = new ExifInterface(url);
                    int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    int rotationInDegrees = Utils.exifToDegrees(rotation);
                    Matrix matrix = new Matrix();
                    if (rotation != 0f) {
                        matrix.preRotate(rotationInDegrees);
                    }
                    Bitmap adjustedBitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                    //First we need to save a crumb of for the selected photo. so we basically treat
                    // this as if we were just saving a photo that had just been taken.
                    String saveImageUrl = LoadBalancer.RequestServerAddress() + "/rest/login/UploadPhoto/"+userId;
                    GETImageSaver imagesave = new GETImageSaver(adjustedBitmap, saveImageUrl, new GETImageSaver.RequestListener() {

                        /*
                         * Override for the onFinished.
                         */
                        @Override
                        public void onFinished(String result) {
                            PreferenceManager.getDefaultSharedPreferences(activityContext).edit().putString("COVERPHOTOID",result).commit();
                            HTTPRequestHandler simpleHttp = new HTTPRequestHandler();
                            simpleHttp.SaveNodeProperty(userId, "CoverPhotoId", result);
                            Intent returnIntent = new Intent();
                            activityContext.setResult(Activity.RESULT_OK,returnIntent);

                            // Quiting on select for now.
                            activityContext.finish();
                        }
                });
                    imagesave.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // Need to save this photo to the server
                // Save our new selection locally too.
                //Intent returnIntent = new Intent();
                //activityContext.setResult(Activity.RESULT_OK,returnIntent);

                // Quiting on select for now.
                activityContext.finish();
            }
        });
    }
}
