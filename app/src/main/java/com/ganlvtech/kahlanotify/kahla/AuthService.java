package com.ganlvtech.kahlanotify.kahla;

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

    public static AuthByPasswordResponse parseAuthByPasswordResponse(Response response) throws IOException, JSONException {
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new AuthByPasswordResponse(json);
    }

    public static InitPusherResponse parseInitPusherResponse(Response response) throws ResponseCodeHttpUnauthorizedException, IOException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        return new InitPusherResponse(json);
    }

    public static VersionResponse parseVersionResponse(Response response) throws IOException, JSONException {
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new VersionResponse(json);
    }

    public static MeResponse parseMeResponse(Response response) throws ResponseCodeHttpUnauthorizedException, IOException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        return new MeResponse(json);
    }

    public Call newAuthByPasswordCall(String email, String password) {
        RequestBody body = new MultipartBody.Builder()
                .addFormDataPart("Email", email)
                .addFormDataPart("Password", password)
                .setType(MultipartBody.FORM)
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

