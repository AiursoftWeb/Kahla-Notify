package com.ganlvtech.kahlanotify.kahla;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class OssService extends BaseService {
    public OssService() {
        super(new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build(), "https://oss.aiursoft.com");
    }

    public OssService(OkHttpClient client, String server) {
        super(client, server);
    }

    public HttpUrl newDownloadFromKeyUrl(int headImgFileKey, int w, int h) {
        return newHttpUrlBuilder("/Download/FromKey")
                .addPathSegment(String.valueOf(headImgFileKey))
                .addQueryParameter("w", String.valueOf(w))
                .addQueryParameter("h", String.valueOf(h))
                .build();
    }

    public HttpUrl newDownloadFromKeyUrl(int headImgFileKey) {
        return newHttpUrlBuilder("/Download/FromKey")
                .addPathSegment(String.valueOf(headImgFileKey))
                .build();
    }

    public String getDownloadFromKeyUrl(int headImgFileKey, int w, int h) {
        return newDownloadFromKeyUrl(headImgFileKey, w, h).toString();
    }

    public String getDownloadFromKeyUrl(int headImgFileKey) {
        return newDownloadFromKeyUrl(headImgFileKey).toString();
    }

    public Call newDownloadFromKeyCall(int headImgFileKey, int w, int h) {
        return newGetCall(newDownloadFromKeyUrl(headImgFileKey, w, h));
    }

    public Call newDownloadFromKeyCall(int headImgFileKey) {
        return newGetCall(newDownloadFromKeyUrl(headImgFileKey));
    }

    public byte[] downloadFromKey(int headImgFileKey, int w, int h) throws IOException, NullPointerException {
        Response response = newDownloadFromKeyCall(headImgFileKey, w, h).execute();
        assert response.body() != null;
        byte[] bytes = response.body().bytes();
        response.close();
        return bytes;
    }
}
