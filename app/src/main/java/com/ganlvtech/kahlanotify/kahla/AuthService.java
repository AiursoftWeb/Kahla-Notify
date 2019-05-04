package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.InitPusherResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthService {
    private OkHttpClient client;
    private String baseUrl;

    public AuthService(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public AuthByPasswordResponse AuthByPassword(String username, String password) throws IOException {
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
        AuthByPasswordResponse r = new AuthByPasswordResponse();
        r.code = -1;
        if (response.body() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                r.code = jsonObject.getInt("code");
                r.message = jsonObject.getString("message");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public InitPusherResponse InitPusher() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/auth/InitPusher")
                .build();
        Response response = client.newCall(request).execute();
        InitPusherResponse r = new InitPusherResponse();
        r.code = -1;
        if (response.body() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                r.code = jsonObject.getInt("code");
                r.message = jsonObject.getString("message");
                if (r.code == 0) {
                    r.channelId = jsonObject.getInt("channelId");
                    r.connectKey = jsonObject.getString("connectKey");
                    r.serverPath = jsonObject.getString("serverPath");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return r;
    }
}

