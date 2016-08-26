package com.teamunemployment.breadcrumbs;

import android.content.Context;

import com.teamunemployment.breadcrumbs.Album.data.Comment;
import com.teamunemployment.breadcrumbs.Network.LoadBalancer;
import com.teamunemployment.breadcrumbs.RESTApi.CrumbService;

import junit.framework.Assert;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author Josiah Kendall.
 */
public class CrumbRemoteRepoTests {

    @Test
    public void TestThatWeCanGetAListOfOneComment() throws IOException {
        Context mockContext = mock(Context.class);
        String result = getTEstDataOneItem();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(mockContext, result, 200)
                ).build();

        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        CrumbService crumbService = retrofit.create(CrumbService.class);
        Call<ArrayList<Comment>> call = crumbService.getCommentsForEvent("1");
        Response<ArrayList<Comment>> commentsResponse = call.execute();
        ArrayList<Comment> comments = commentsResponse.body();
        assertTrue(comments.size() == 1);
        assertTrue(comments.get(0).getId().equals("1"));
    }

    @Test
    public void TEstThatWeCanGetAListWithMoreThanOneItem() throws IOException {
        Context mockContext = mock(Context.class);
        String result = getTEstData2Items();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(
                        new MockClient(mockContext, result, 200)
                ).build();

        Retrofit retrofit = new Retrofit.Builder()
                // .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(LoadBalancer.RequestServerAddress() + "/rest/")
                .client(okHttpClient)
                .build();

        CrumbService crumbService = retrofit.create(CrumbService.class);
        Call<ArrayList<Comment>> call = crumbService.getCommentsForEvent("1");
        Response<ArrayList<Comment>> commentsResponse = call.execute();
        ArrayList<Comment> comments = commentsResponse.body();
        assertTrue(comments.get(0).getId().equals("1"));
        assertTrue(comments.get(1).getId().equals("6"));
    }

    private String getTEstData2Items() {
        return "[{\"Id\":\"1\",\"UserId\" :\"0\",\"CommentText\":\"This is a test Comment\", \"EntityId\":\"3\"},{\"Id\":\"6\",\"UserId\" :\"0\",\"CommentText\":\"This is a test Comment\", \"EntityId\":\"3\"}]";
    }

    private String getTEstDataOneItem() {
            return "[{\"Id\":\"1\",\"UserId\" :\"0\",\"CommentText\":\"This is a test Comment\", \"EntityId\":\"3\"}]";
    }
}
