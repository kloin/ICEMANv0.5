package com.teamunemployment.breadcrumbs.client.tabs.LocalPhotos;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.GETImageSaver;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.R;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.Trails.EditMyTrail;
import com.teamunemployment.breadcrumbs.client.Adapters.LocalFilesGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.Cards.FolderChooserGridViewAdapter;
import com.teamunemployment.breadcrumbs.client.ImageChooser.GalleryFolder;
import com.teamunemployment.breadcrumbs.client.ImageChooser.GalleryManager;
import com.teamunemployment.breadcrumbs.client.ImageChooser.GridImageSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jek40 on 6/04/2016.
 */
public class LocalFoldersTab extends GridImageSelector {
    private TextView emptyGridInfo;

    @Override
    public void setUpGridAndListeners() {
        //String facebookId = PreferenceManager.getDefaultSharedPreferences(activityContext).getString("FACEBOOK_REGISTRATION_ID", "-1");
        //if (facebookId.equals("-1")) {
        //  return;
        // }
        emptyGridInfo = (TextView) rootView.findViewById(R.id.empty_grid_placeholder);
        loadLocalFolders();
    }

    private void loadLocalFolders() {
        final GridView gridview = (GridView) rootView.findViewById(R.id.gridView1);
        final ArrayList<String> ids = new ArrayList<>();
        GalleryManager galleryManager = new GalleryManager(context);
        final HashMap<String, GalleryFolder> hashMap = galleryManager.GetGalleryFolders();

        FolderChooserGridViewAdapter adapter = new FolderChooserGridViewAdapter(hashMap, activityContext);
        gridview.setAdapter(adapter);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] keys = hashMap.keySet().toArray(new String[hashMap.size()]);
                GalleryFolder folder = hashMap.get(keys[position]);
                // Launch the application here.
                Intent intent = new Intent();
                intent.setClassName("com.teamunemployment.breadcrumbs", "com.teamunemployment.breadcrumbs.client.ImageChooser.LocalPhotoImageSelector");
                intent.putStringArrayListExtra("Images",folder.Images);
                startActivityForResult(intent, 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 5) {
            Activity activityContext = (Activity) context;
            Intent returnIntent = new Intent();
            activityContext.setResult(Activity.RESULT_OK, returnIntent);
            activityContext.finish();

        }
    }
}
