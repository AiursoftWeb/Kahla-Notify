package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.InitPusherResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.VersionResponse;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthService extends BaseService {
    public AuthService(OkHttpClient client, String server) {
        super(client, server);
    }

    @NonNull
    public static AuthByPasswordResponse parseAuthByPasswordResponse(@NonNull Response response) throws IOException, JSONException {
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new AuthByPasswordResponse(json);
    }

    @NonNull
    public static InitPusherResponse parseInitPusherResponse(@NonNull Response response) throws ResponseCodeHttpUnauthorizedException, IOException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        return new InitPusherResponse(json);
    }

    @NonNull
    public static VersionResponse parseVersionResponse(@NonNull Response response) throws IOException, JSONException {
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new VersionResponse(json);
    }

    @NonNull
    public static MeResponse parseMeResponse(@NonNull Response response) throws ResponseCodeHttpUnauthorizedException, IOException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        return new MeResponse(json);
    }

    public Call newAuthByPasswordCall(String email, String password) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Email", email)
                .addFormDataPart("Password", password)
                .build();
        return newCall(newRequestBuilder("/Auth/AuthByPassword")
                .post(body)
                .build());
    }

    public Call newInitPusherCall() {
        return newGetCall("/Auth/InitPusher");
    }

    public Call newVersionCall() {
        return newGetCall("/Auth/Version");
    }

    public Call newMeCall() {
        return newGetCall("/Auth/Me");
    }

    public AuthByPasswordResponse AuthByPassword(String email, String password) throws IOException, JSONException {
        return parseAuthByPasswordResponse(newAuthByPasswordCall(email, password).execute());
    }

    public InitPusherResponse InitPusher() throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return parseInitPusherResponse(newInitPusherCall().execute());
    }

    public VersionResponse Version() throws IOException, JSONException {
        return parseVersionResponse(newVersionCall().execute());
    }

    public MeResponse Me() throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return parseMeResponse(newMeCall().execute());
    }
}

