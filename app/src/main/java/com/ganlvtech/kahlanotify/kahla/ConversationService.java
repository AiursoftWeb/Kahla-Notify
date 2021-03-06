package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.SendMessageResponse;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConversationService extends BaseService {
    public ConversationService(OkHttpClient client, String server) {
        super(client, server);
    }

    @NonNull
    public static GetMessageResponse parseGetMessageResponse(@NonNull Response response) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new GetMessageResponse(json);
    }

    @NonNull
    public static SendMessageResponse parseSendMessageResponse(@NonNull Response response) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new SendMessageResponse(json);
    }

    public Call newGetMessageCall(int id, int skipTill, int take) {
        HttpUrl url = newHttpUrlBuilder("/Conversation/GetMessage")
                .addPathSegment(String.valueOf(id))
                .addQueryParameter("skipTill", String.valueOf(skipTill))
                .addQueryParameter("take", String.valueOf(take))
                .build();
        return newGetCall(url);
    }

    public Call newSendMessageCall(int id, String content) {
        HttpUrl url = newHttpUrlBuilder("/Conversation/SendMessage")
                .addPathSegment(String.valueOf(id))
                .build();
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("content", content)
                .build();
        return newCall(newRequestBuilder(url)
                .post(body)
                .build());
    }

    public GetMessageResponse GetMessage(int id) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return GetMessage(id, -1, 15);
    }

    public GetMessageResponse GetMessage(int id, int skipTill, int take) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return parseGetMessageResponse(newGetMessageCall(id, skipTill, take).execute());
    }

    public SendMessageResponse SendMessage(int id, String content) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return parseSendMessageResponse(newSendMessageCall(id, content).execute());
    }
}

