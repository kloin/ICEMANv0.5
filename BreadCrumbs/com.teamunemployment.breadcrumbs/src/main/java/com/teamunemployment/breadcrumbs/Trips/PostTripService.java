package com.teamunemployment.breadcrumbs.Trips;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by jek40 on 6/07/2016.
 */
public class PostTripService {

    private OkHttpClient client = new OkHttpClient();
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public void SaveCoverPhoto(String coverPhotoId, String trailId) {
        String url = LoadBalancer.RequestServerAddress() + "/rest/login/SaveStringPropertyToNode/"+trailId+"/CoverPhotoId/"+coverPhotoId;
        try {
            simpleHttpRequest(url);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String simpleHttpRequest(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
