package com.teamunemployment.breadcrumbs;

import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.NodeService;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author Josiah Kendall
 */
public class NodeServiceTests {

    @Test
    public void TestThatAddingViewsWorks() throws IOException {
        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .build();
        NodeService nodeService = retrofit.create(NodeService.class);
        Call<ResponseBody> responseCall = nodeService.addTrailView("24437");
        Response<ResponseBody> responseBodyResponse = responseCall.execute();
        Assert.assertTrue(responseBodyResponse.code() == 200);
    }
}
