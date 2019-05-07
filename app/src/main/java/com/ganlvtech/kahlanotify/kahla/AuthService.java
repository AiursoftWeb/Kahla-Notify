package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.InitPusherResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.VersionResponse;

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

    public AuthByPasswordResponse AuthByPassword(String email, String password) throws IOException, JSONException {
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("Email", email)
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
            JSONObject jsonObject = new JSONObject(response.body().string());
            r.code = jsonObject.getInt("code");
            r.message = jsonObject.getString("message");
        }
        return r;
    }

    public InitPusherResponse InitPusher() throws IOException, ResponseCodeHttpUnauthorizedException {
        Request request = new Request.Builder()
                .url(baseUrl + "/Auth/InitPusher")
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 401) {
            throw new ResponseCodeHttpUnauthorizedException();
        }
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

    public VersionResponse Version() throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/Auth/Version")
                .build();
        Response response = client.newCall(request).execute();
        VersionResponse r = new VersionResponse();
        r.code = -1;
        if (response.body() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                r.code = jsonObject.getInt("code");
                r.message = jsonObject.getString("message");
                if (r.code == 0) {
                    r.latestVersion = jsonObject.optString("latestVersion");
                    r.oldestSupportedVersion = jsonObject.optString("oldestSupportedVersion");
                    r.downloadAddress = jsonObject.optString("downloadAddress");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public MeResponse Me() throws IOException, ResponseCodeHttpUnauthorizedException {
        Request request = new Request.Builder()
                .url(baseUrl + "/Auth/Me")
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 401) {
            throw new ResponseCodeHttpUnauthorizedException();
        }
        MeResponse r = new MeResponse();
        r.code = -1;
        if (response.body() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                r.code = jsonObject.getInt("code");
                r.message = jsonObject.getString("message");
                JSONObject jsonObjectValue = jsonObject.getJSONObject("value");
                if (r.code == 0) {
                    r.value = new User();
                    r.value.accountCreateTime = jsonObjectValue.getString("accountCreateTime");
                    r.value.bio = jsonObjectValue.isNull("bio") ? "" : jsonObjectValue.getString("bio");
                    r.value.email = jsonObjectValue.getString("email");
                    r.value.emailConfirmed = jsonObjectValue.getBoolean("emailConfirmed");
                    r.value.enableEmailNotification = jsonObjectValue.getBoolean("enableEmailNotification");
                    r.value.headImgFileKey = jsonObjectValue.getInt("headImgFileKey");
                    r.value.id = jsonObjectValue.getString("id");
                    r.value.makeEmailPublic = jsonObjectValue.getBoolean("makeEmailPublic");
                    r.value.nickName = jsonObjectValue.getString("nickName");
                    r.value.preferedLanguage = jsonObjectValue.getString("preferedLanguage");
                    r.value.sex = jsonObjectValue.getString("sex");
                    r.value.themeId = jsonObjectValue.getInt("themeId");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

}

