package com.teamunemployment.breadcrumbs;

import android.content.Context;
import android.databinding.tool.util.L;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by jek40 on 23/07/2016.
 */
public class MockClient implements Interceptor {

    private Context context;
    private String content;
    private int code;

    public MockClient(Context ctx, String content, int code) {
        this.context = ctx;
        this.content = content;
        this.code = code;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String mimeType = "application/json";

        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .body(ResponseBody.create(MediaType.parse(mimeType), content))
                .build();
    }
}