package com.ganlvtech.kahlanotify.kahla;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OssService {
    private OkHttpClient client;
    private String baseUrl;

    public OssService(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public String getDownloadFromKeyUrl(int headImgFileKey, int w, int h) {
        HttpUrl url = HttpUrl.get(baseUrl).newBuilder()
                .addPathSegments("/Download/FromKey")
                .addPathSegment(String.valueOf(headImgFileKey))
                .addQueryParameter("w", String.valueOf(w))
                .addQueryParameter("h", String.valueOf(h))
                .build();
        return url.toString();
    }

    public byte[] downloadFromKey(int headImgFileKey, int w, int h) throws IOException, NullPointerException {
        HttpUrl url = HttpUrl.get(baseUrl).newBuilder()
                .addPathSegments("/Download/FromKey")
                .addPathSegment(String.valueOf(headImgFileKey))
                .addQueryParameter("w", String.valueOf(w))
                .addQueryParameter("h", String.valueOf(h))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        return responseBody.bytes();
    }
}
