package com.ganlvtech.kahlanotify;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class KahlaWebApiClient {
    private OkHttpClient client;
    private String baseUrl;

    public KahlaWebApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
        CookieJar cookieJar = new CookieJar() {
            private final Map<String, List<Cookie>> cookiesMap = new HashMap<String, List<Cookie>>();

            @Override
            public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                String host = url.host();
                List<Cookie> cookiesList = cookiesMap.get(host);
                if (cookiesList != null) {
                    cookiesMap.remove(host);
                }
                cookiesMap.put(host, cookies);
            }

            @Override
            public List<Cookie> loadForRequest(HttpUrl url) {
                List<Cookie> cookiesList = cookiesMap.get(url.host());
                return cookiesList != null ? cookiesList : new ArrayList<Cookie>();
            }
        };
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .cookieJar(cookieJar)
                .build();
    }

    public boolean Login(String username, String password) throws IOException, JSONException {
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("Email", username)
                .addFormDataPart("Password", password)
                .setType(MultipartBody.FORM)
                .build();
        Request request = new Request.Builder()
                .url(baseUrl + "/Auth/AuthByPassword")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null) {
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                return true;
            }
        }
        return false;
    }

    public String getWebSocketUrl() throws IOException, JSONException {
        Request request = new Request.Builder()
                .url(baseUrl + "/auth/InitPusher")
                .build();
        Response response = client.newCall(request).execute();
        if (response.body() != null) {
            String responseBody = response.body().string();
            JSONObject jsonObject = new JSONObject(responseBody);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                String serverPath = jsonObject.getString("serverPath");
                return serverPath;
            }
        }
        return null;
    }
}
