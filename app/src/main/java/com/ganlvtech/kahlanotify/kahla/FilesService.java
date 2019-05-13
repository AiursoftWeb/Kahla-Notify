package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.responses.files.UploadFileResponse;
import com.ganlvtech.kahlanotify.kahla.responses.files.UploadMediaResponse;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FilesService extends BaseService {
    public FilesService(OkHttpClient client, String server) {
        super(client, server);
    }

    @NonNull
    public static UploadMediaResponse parseUploadMediaResponse(@NonNull Response response) throws IOException, JSONException, ResponseCodeHttpUnauthorizedException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new UploadMediaResponse(json);
    }

    @NonNull
    public static UploadFileResponse parseUploadFileResponse(@NonNull Response response) throws IOException, JSONException, ResponseCodeHttpUnauthorizedException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new UploadFileResponse(json);
    }

    public Call newUploadMediaCall(byte[] bytes, String fileName) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(null, bytes))
                .build();
        return newCall(newRequestBuilder("/Files/UploadMedia")
                .post(body)
                .build());
    }

    public Call newUploadFileCall(int conversationId, byte[] bytes, String fileName) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName, RequestBody.create(MediaType.parse("application/octet-stream"), bytes))
                .build();
        return newCall(newRequestBuilder(
                newHttpUrlBuilder("/Files/UploadFile")
                        .addQueryParameter("ConversationId", String.valueOf(conversationId))
                        .build())
                .post(body)
                .build());
    }

    public UploadMediaResponse UploadMedia(byte[] bytes, String fileName) throws IOException, JSONException, ResponseCodeHttpUnauthorizedException {
        return parseUploadMediaResponse(newUploadMediaCall(bytes, fileName).execute());
    }

    public UploadFileResponse UploadFile(int conversationId, byte[] file, String fileName) throws IOException, JSONException, ResponseCodeHttpUnauthorizedException {
        return parseUploadFileResponse(newUploadFileCall(conversationId, file, fileName).execute());
    }
}

