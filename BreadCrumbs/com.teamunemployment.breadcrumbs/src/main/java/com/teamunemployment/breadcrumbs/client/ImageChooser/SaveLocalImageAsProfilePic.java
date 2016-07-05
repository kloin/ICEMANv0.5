package com.teamunemployment.breadcrumbs.client.ImageChooser;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.UploadFile;
import com.teamunemployment.breadcrumbs.Profile.data.LocalProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.ProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteProfileRepository;
import com.teamunemployment.breadcrumbs.Profile.data.RemoteRepositoryFactory;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;
import com.teamunemployment.breadcrumbs.client.Image.ImageLoadingManager;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author Josiah Kendall.
 *
 * Intent Service to handle the save of a profile picture.
 */
public class SaveLocalImageAsProfilePic extends IntentService {
    public SaveLocalImageAsProfilePic() {
        super("SaveLocalImageUploadService");
    }
    public SaveLocalImageAsProfilePic(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String id = intent.getStringExtra("Id");
        String userId = intent.getStringExtra("UserId");
        // Root of the local images folder.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        final Uri uri = Uri.parse(images + "/" +id);
        // FetchMedia
        ImageLoadingManager imageLoadingManager = new ImageLoadingManager(this);
        Bitmap bitmap = null;
        try {
            bitmap = imageLoadingManager.GetFull720Bitmap(uri);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        String url = LoadBalancer.RequestServerAddress() + "/rest/Crumb/UploadProfilePictureForUser/"+userId;

        byte[] bites = Utils.ConvertBitmapToByteArray(bitmap);


        MediaType MEDIA_TYPE = MediaType.parse("image/jpg");

        // Build up and send response
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", "data", RequestBody.create(MEDIA_TYPE, bites))
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        try {
            Response response = client.newCall(request).execute();

            // Wait on response because we need to save the new profile picture Id.
            if (response.code() == 200) {
                DatabaseController databaseController = new DatabaseController(this);
                LocalProfileRepository localProfileRepository = new LocalProfileRepository(databaseController);
                RemoteProfileRepository remoteProfileRepository = RemoteRepositoryFactory.GetRemoteProfileRepository();
                ProfileRepository profileRepository = new ProfileRepository(localProfileRepository, remoteProfileRepository);
                profileRepository.saveProfilePictureId(response.body().string(), Long.parseLong(userId));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
