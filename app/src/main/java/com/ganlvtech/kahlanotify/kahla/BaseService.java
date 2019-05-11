package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BaseService {
    protected OkHttpClient mClient;
    protected String mServer;

    public BaseService(OkHttpClient client, String server) {
        mClient = client;
        mServer = server;
    }

    protected Call newCall(Request request) {
        return mClient.newCall(request);
    }

    protected HttpUrl.Builder newHttpUrlBuilder(String path) {
        return HttpUrl.get(mServer).newBuilder()
                .addPathSegments(path);
    }

    protected Request.Builder newRequestBuilder(String path) {
        return new Request.Builder()
                .url(newHttpUrlBuilder(path).build());
    }

    protected Request.Builder newRequestBuilder(HttpUrl url) {
        return new Request.Builder()
                .url(url);
    }

    protected Call newGetCall(HttpUrl url) {
        return newCall(newRequestBuilder(url).build());
    }

    protected Call newGetCall(String path) {
        return newCall(newRequestBuilder(path).build());
    }

    @NonNull
    protected static Response mustAuthorized(@NonNull Response response) throws ResponseCodeHttpUnauthorizedException {
        if (response.code() == 401) {
            response.close();
            throw new ResponseCodeHttpUnauthorizedException();
        }
        return response;
    }
}
