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
 * @author Josiah Kendall
 */
public class LocalPhotoImageSelector extends Activity {
    private View lastClickedView = null;
    private Activity mContext;
    private ArrayList<String> ids;
    private static final String TAG = "ImageUpload";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_image_layout_wrapper_with_toolbar);
        mContext = this;
        ids = getIntent().getStringArrayListExtra("Images");
        displayImages();
        setUpButtonListeners();
    }

    /**
     * Set the back button listener.
     */
    private void setUpButtonListeners() {
        ImageView backButton = (ImageView) findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    /**
     * display the thumbnails in the grid.
     */
    private void displayImages() {
        // Placeholder - incase we have no ids (images) to display.
        TextView emptyGridInfo = (TextView) findViewById(R.id.empty_grid_placeholder);
        if(ids.size() == 0) {
            emptyGridInfo.setVisibility(View.VISIBLE);
            SimpleAnimations simpleAnimations = new SimpleAnimations();
            simpleAnimations.FadeInView(emptyGridInfo);
        }

        // Grab the gridview and set our custom adapter.
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

    /**
     * Upload an image to the server.
     * @param position The position in the {@link #ids} dataset.
     * @param url the url that we are
     */
    private void uploadImage(int position, String url) {

        // BAND AID FOR POOR CODE
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Uri uri = Uri.parse(images + "/" + ids.get(position));
        String stringUrl = getRealPathFromURI(mContext, uri);

        // Request listener just there to prevent a nullPointer exception.
        UploadFile imagesave = new UploadFile(url, stringUrl, new UploadFile.RequestListener() {
            @Override
            public void onFinished(String result) {
            }
        });
        imagesave.execute();
    }

    /**
     * Construct a real, usable string path from a {@link Uri}. Used because sometimes we need to
     * fetch using a string path, and the {@link Uri#toString()} doesnt work.
     * @param context Our context
     * @param contentUri The {@link Uri} that we are converting to a string path.
     * @return The path to the file represented by the {@Uri}.
     */
    private String getRealPathFromURI(Context context, Uri contentUri) {
        String[] proj = { MediaStore.Audio.Media.DATA };
        CursorLoader loader = new CursorLoader(context, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    /**
     * Upload our profile image to the server.
     * @param position The position of the saved item in the dataset. Used to find
     *                 the id of the image so that we can grab it and send it to the server.
     */
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

    /**
     * Construct a request {@link com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval.RequestListener}
     * for handling the callback of saving the new coverphotoid.
     * @param userId The userId we are saving the image for
     * @param position The position we are in the dataset.
     * @return A new {@link com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval.RequestListener}.
     */
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

    /**
     * Add the tick to the image that we are selecting
     * @param gridview The grid item (photo) that we are going to add a tick to.
     */
    private void setItemAsClicked(View gridview) {

        gridview.findViewById(R.id.tick_for_image_select).setVisibility(View.VISIBLE);
        if (lastClickedView != null) {
            lastClickedView.findViewById(R.id.tick_for_image_select).setVisibility(View.GONE);
        }
        lastClickedView = gridview;
    }
}
