package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncImageFetch;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.GETImageSaver;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UploadFile;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.caching.GlobalContainer;
import com.teamunemployment.breadcrumbs.caching.TextCaching;
import com.teamunemployment.breadcrumbs.client.Adapters.LocalFilesGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.Animations.SimpleAnimations;
import com.teamunemployment.breadcrumbs.client.Image.ImageLoadingManager;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by jek40 on 7/04/2016.
 */
public class LocalPhotoImageSelector extends Activity {
    private int currentlySelectedPosition;
    private View lastClickedView = null;
    private Activity mContext;
    private GalleryFolder mGalleryFolder;
    private ArrayList<String> ids;
    private static final String TAG = "Image Upload:";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_image_layout_wrapper_with_toolbar);
        mContext = this;
        //setShitUp();
        ids = getIntent().getStringArrayListExtra("Images");
        displayImages();
        setUpButtonListeners();
    }

    private void setUpButtonListeners() {
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void displayImages() {
        TextView emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        if(ids.size() == 0) {
            emptyGridInfo.setVisibility(View.VISIBLE);
            SimpleAnimations simpleAnimations = new SimpleAnimations();
            simpleAnimations.FadeInView(emptyGridInfo);
        }

        final GridView gridview = (GridView)  findViewById(R.id.gridView1);
        LocalFilesGridViewAdapter adapter = new LocalFilesGridViewAdapter(ids, mContext);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, final int position, long id) {
                setItemAsClicked(v);
                TextView textView = (TextView) findViewById(R.id.select_button);
                textView.setVisibility(View.VISIBLE);
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Need to save this photo to the server
                        // Save our new selection locally too.
                        //Intent returnIntent = new Intent();
                        //activityContext.setResult(Activity.RESULT_OK,returnIntent);
                        //UploadTheImage(position);
                        UploadProfileImage(position);
                        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        final Uri uri = Uri.parse(images + "/" + ids.get(position));

                        // Quiting on select for now.
                        ImageLoadingManager imageLoadingManager = new ImageLoadingManager(mContext);
                        Bitmap bitmap = null;
                        try {
                            bitmap = imageLoadingManager.GetFull720Bitmap(uri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        GlobalContainer.GetContainerInstance().SetBitMap(bitmap);
                        // Tell the tabs activity (from previously) that we have saved and want to go back to the profile page.
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("bitmap",bitmap);
                        mContext.setResult(5, returnIntent);
                        mContext.finish();
                        //mContext.finish();
                    }
                });
            }
        });
    }

    private void uploadImage(int position, String url) {

        // BAND AID FOR POOR CODE
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Uri uri = Uri.parse(images + "/" + ids.get(position));
        String stringUrl = getRealPathFromURI(mContext, uri);
        File sourceFile = new File(stringUrl);
        UploadFile imagesave = new UploadFile(url, stringUrl, new UploadFile.RequestListener() {

            /*
             * Override for the
             */
            @Override
            public void onFinished(String result) {
            }
        });
        imagesave.execute();
    }

    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void UploadProfileImage(int position) {

        // Root of the local images folder.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        // FetchMedia
        final String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");

        // First we save the crumb to the database. We use a request listerner (getRequestListener) to  do the save once we have updated the profile image.
        final String saveImageUrl = LoadBalancer.RequestServerAddress() + "/rest/Crumb/UploadProfileImage/"+ userId;
        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(saveImageUrl, getRequestListener(userId, position), mContext);
        asyncDataRetrieval.execute();

        final Uri uri = Uri.parse(images + "/" + ids.get(position));

        // FetchMedia
        ImageLoadingManager imageLoadingManager = new ImageLoadingManager(mContext);
        Bitmap bitmap = null;
        try {
            bitmap = imageLoadingManager.GetFull720Bitmap(uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Tell the tabs activity (from previously) that we have saved and want to go back to the profile page.
        Intent returnIntent = new Intent();
        returnIntent.putExtra("bitmap",bitmap);
        mContext.setResult(5, returnIntent);
        mContext.finish();
    }

    private AsyncDataRetrieval.RequestListener getRequestListener(final String userId, final int position) {
        return new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) throws JSONException {
                Log.d(TAG, "Created image save with Id of: "+result);
                //First we need to save a crumb of for the selected photo. so we basically treat
                // this as if we were just saving a photo that had just been taken.
                String url = new String(LoadBalancer.RequestServerAddress() + "/rest/login/savecrumb/"+ result);
                uploadImage(position, url);
                // Save our cover photo for opening a few activities later. When we return to profile page.
                PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("COVERPHOTOID",result).commit();
                HTTPRequestHandler simpleHttp = new HTTPRequestHandler();
                simpleHttp.SaveNodeProperty(userId, "CoverPhotoId", result, mContext);
                Intent returnIntent = new Intent();
                mContext.setResult(5, returnIntent);
                mContext.finish();
            }
        };
    }

    private void UploadTheImage(int position) {
        try {
            // Save our profile header pic.
            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            final Uri uri = Uri.parse(images + "/" + ids.get(position));

            // FetchMedia
            final String userId = PreferenceManager.getDefaultSharedPreferences(mContext).getString("USERID", "-1");
            ImageLoadingManager imageLoadingManager = new ImageLoadingManager(mContext);
            final Bitmap bitmap = imageLoadingManager.GetFull720Bitmap(uri);

            // Save image to disk. This is done because I need to have a file to send the image via
            // the gihub library for mutipart sending as a service that w are using to upload images.
            final String file = com.teamunemployment.breadcrumbs.caching.Utils.getExternalCacheDir(mContext).getAbsolutePath();

            final String otherFile = Utils.writeBitmapToDisk(bitmap, file+"/breadcrumbsprofile.png");

            // Send url to save the Image Model
            final String saveImageUrl = LoadBalancer.RequestServerAddress() + "/rest/Crumb/UploadProfileImage/"+ userId;
            AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(saveImageUrl, new AsyncDataRetrieval.RequestListener() {
                @Override
                public void onFinished(String result) throws JSONException {
                    Log.d(TAG, "Created image save with Id of: "+result);
                    //First we need to save a crumb of for the selected photo. so we basically treat
                    // this as if we were just saving a photo that had just been taken.
                    String uploadImage= LoadBalancer.RequestServerAddress() + "/rest/login/savecrumb/"+result;
                    try {
                        new MultipartUploadRequest(mContext, uploadImage)
                                .addFileToUpload(file + "/breadcrumbsprofile.png", "test")
                                        .setNotificationConfig(new UploadNotificationConfig())
                                        .setMaxRetries(2)
                                        .startUpload();
                    } catch (Exception exc) {
                        Log.e("AndroidUploadService", exc.getMessage(), exc);
                    }

                    // Save our cover photo for opening a few activities later. When we return to profile page.
                    PreferenceManager.getDefaultSharedPreferences(mContext).edit().putString("COVERPHOTOID",result).commit();
                    HTTPRequestHandler simpleHttp = new HTTPRequestHandler();
                    simpleHttp.SaveNodeProperty(userId, "CoverPhotoId", result, mContext);
                    Intent returnIntent = new Intent();
                    mContext.setResult(5, returnIntent);
                    // Quiting on select for now.
                   // mContext.finish();
                }
            }, mContext);
            asyncDataRetrieval.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setItemAsClicked(View gridview) {

        gridview.findViewById(R.id.tick_for_image_select).setVisibility(View.VISIBLE);
        if (lastClickedView != null) {
            lastClickedView.findViewById(R.id.tick_for_image_select).setVisibility(View.GONE);
        }
        lastClickedView = gridview;
    }
}
