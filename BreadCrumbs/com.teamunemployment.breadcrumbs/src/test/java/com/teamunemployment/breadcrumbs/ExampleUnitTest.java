package com.teamunemployment.breadcrumbs;

import android.content.ContentValues;
import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreLocalRepository;
import com.teamunemployment.breadcrumbs.Explore.Data.ExploreRemoteRepository;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;
import com.teamunemployment.breadcrumbs.RESTApi.UserService;
import com.teamunemployment.breadcrumbs.Trails.Trip;
import com.teamunemployment.breadcrumbs.database.DatabaseController;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private DatabaseController mDbc;
    private Context context;

    @Before
    public void setup() {
        context = Mockito.mock(Context.class);
       // mDbc = new DatabaseController(context);
    }

    private String generateMockJSON() throws JSONException {
        return "{\"StartDate\":\"2016-06-01\",\"CoverPhotoId\":\"17826\",\"Views\":\"0\",\"Description\":\" \",\"UserId\":\"11136\",\"TrailName\":\"Raglan trip\",\"Id\":\"17734\",\"Distance\":\"0\"}";
    }

    @Test
    public void TestThatWeCanLoadTrip() throws JSONException {

        String result = generateMockJSON();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(context, result, 200)
                ).build();

        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        UserService userService = retrofit.create(UserService.class);
        NodeService nodeService = retrofit.create(NodeService.class);

        ExploreRemoteRepository remoteRepository = new ExploreRemoteRepository(userService, nodeService);
        Trip trip = remoteRepository.LoadTrip(0);

        Assert.assertTrue(trip.getTrailName().equals("Raglan trip"));
    }

    @Test
    public void TestThatLoadingBadTripfailsQuietly() throws JSONException {
        String result = "{}";
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(context, result, 200)
                ).build();


        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        UserService userService = retrofit.create(UserService.class);
        NodeService nodeService = retrofit.create(NodeService.class);

        ExploreRemoteRepository remoteRepository = new ExploreRemoteRepository(userService, nodeService);
        Trip trip = remoteRepository.LoadTrip(0);

        Assert.assertTrue(trip !=null);
    }








}