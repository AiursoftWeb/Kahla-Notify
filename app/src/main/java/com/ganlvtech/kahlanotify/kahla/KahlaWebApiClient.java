package com.ganlvtech.kahlanotify.kahla;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String responseBodyString = responseBody.string();
            JSONObject jsonObject = new JSONObject(responseBodyString);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                return true;
            }
        }
        return false;
    }

    public JSONArray getMyFriends() throws IOException, JSONException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        Request request = new Request.Builder()
                .url(baseUrl + "/friendship/MyFriends?orderByName=false")
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();
        if (responseBody != null) {
            String responseBodyString = responseBody.string();
            JSONObject jsonObject = new JSONObject(responseBodyString);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONArray items = jsonObject.getJSONArray("items");
                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    String aesKey = item.getString("aesKey");
                    String latestMessage = item.getString("latestMessage");
                    if (latestMessage != null) {
                        byte[] bytes = CryptoJs.aesDecrypt(latestMessage, aesKey);
                        String latestMessageDecrypted = new String(bytes, "UTF-8");
                        item.put("latestMessageDecrypted", latestMessageDecrypted);
                    }
                }
                return jsonObject.getJSONArray("items");
            }
        }
        return null;
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
