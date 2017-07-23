package com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import com.teamunemployment.breadcrumbs.Album.LocalAlbumSummary.Data.LocalAlbumSummaryRepo;
import com.teamunemployment.breadcrumbs.Album.repo.LocalAlbumRepo;
import com.teamunemployment.breadcrumbs.BackgroundServices.UploadTrailService;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.AsyncDataRetrieval;
import com.teamunemployment.breadcrumbs.Network.ServiceProxy.HTTPRequestHandler;
import com.teamunemployment.breadcrumbs.PreferencesAPI;
import com.teamunemployment.breadcrumbs.RandomUsefulShit.Utils;

import java.text.MessageFormat;

import javax.inject.Inject;

/**
 * @author Josiah Kendall
 */
public class LocalAlbumModel {

    private LocalAlbumSummaryRepo localAlbumSummaryRepo;
    private PreferencesAPI preferencesAPI;
    private Context context;

    public LocalAlbumModel(Context context, LocalAlbumSummaryRepo localAlbumSummaryRepo, PreferencesAPI preferencesAPI) {
        // default constructor.
        this.localAlbumSummaryRepo = localAlbumSummaryRepo;
        this.preferencesAPI = preferencesAPI;
        this.context = context;
    }

    public LocalAlbumModel(LocalAlbumSummaryRepo localAlbumSummaryRepo) {
        this.localAlbumSummaryRepo = localAlbumSummaryRepo;
    }

    public Bitmap LoadCoverBitmap() {
        String coverPhoto = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/coverphoto.jpg";
        return Utils.FetchRawBitmapFromFile(coverPhoto);
    }

    public void SaveBitmap(Bitmap bitmap) {
        // save our coverphoto to file.
    }

    public String LoadTitle() {
        return localAlbumSummaryRepo.getAlbumTitle();
    }

    public boolean LoadPublicity() {
        return localAlbumSummaryRepo.getIsPublic();
    }

    public void SaveTitle(String title) {
        localAlbumSummaryRepo.SaveAlbumTitle(title);
    }

    public void SavePublicity(boolean isPublic) {
        localAlbumSummaryRepo.SaveAlbumPublicity(isPublic);
    }

    public void publishAlbum(final String trailName, String userId) {

        String url = MessageFormat.format("{0}/rest/login/saveTrail/{1}/{2}/{3}",
                LoadBalancer.RequestServerAddress(),
                trailName,
                " ",
                userId);

        // Save
        Log.d("UPLOAD", "Attempting to create a new Trail with url: " + url);
        url = url.replaceAll(" ", "%20");

        AsyncDataRetrieval asyncDataRetrieval = new AsyncDataRetrieval(url, new AsyncDataRetrieval.RequestListener() {
            @Override
            public void onFinished(String result) {
                //result is the id of the trail we just saved.
                preferencesAPI.SaveCurrentServerTrailId(Integer.parseInt(result));
                publishTrail(result, trailName);
            }
        }, context);
        asyncDataRetrieval.execute();
    }

    /**
     * Push the changes for a trail to the server
     * @param trailId
     * @return The trail publish service was successfully launched.
     */
    private boolean publishTrail(String trailId, String trailName) {

        // This is the use case where the trail title has no data. This is an issue an we cannot save
        if (trailName.isEmpty()) {
            return false;
        }

        int serverTrailId = preferencesAPI.GetServerTrailId();
        if (serverTrailId == -1) {
            // Issue, it should have this at this point
            return false;
        }

        // Save our name locally for future reference.
        preferencesAPI.SaveTrailNameString(trailName);

        // Save the trail name to the server too.
        HTTPRequestHandler httpRequestHandler = new HTTPRequestHandler();
        httpRequestHandler.SaveNodeProperty(Integer.toString(serverTrailId), "TrailName", trailName, context);

        Intent uploadTrailService = new Intent(context, UploadTrailService.class);
        context.startService(uploadTrailService);

        return true;
    }
}

