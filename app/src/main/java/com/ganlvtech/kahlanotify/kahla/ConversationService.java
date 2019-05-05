package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ConversationService {
    private OkHttpClient client;
    private String baseUrl;

    public ConversationService(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public GetMessageResponse GetMessage(int id) throws IOException {
        return GetMessage(id, -1, 15);
    }

    public GetMessageResponse GetMessage(int id, int skipTill, int take) throws IOException {
        HttpUrl url = HttpUrl.get(baseUrl).newBuilder()
                .addPathSegments("/Conversation/GetMessage")
                .addPathSegment(String.valueOf(id))
                .addQueryParameter("skipTill", String.valueOf(skipTill))
                .addQueryParameter("take", String.valueOf(take))
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        GetMessageResponse r = new GetMessageResponse();
        r.code = -1;
        if (response.body() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.body().string());
                r.code = jsonObject.getInt("code");
                r.message = jsonObject.getString("message");
                if (r.code == 0) {
                    r.items = new ArrayList<>();
                    JSONArray jsonArray = jsonObject.getJSONArray("items");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        Message item = new Message();
                        JSONObject jsonArrayItem = jsonArray.getJSONObject(i);
                        item.conversationId = jsonArrayItem.getInt("conversationId");
                        item.id = jsonArrayItem.getInt("id");
                        item.senderId = jsonArrayItem.getString("senderId");
                        JSONObject jsonArrayItemSender = jsonArrayItem.getJSONObject("sender");
                        item.sender = new User();
                        item.sender.accountCreateTime = jsonArrayItemSender.getString("accountCreateTime");
                        item.sender.bio = jsonArrayItemSender.getString("bio");
                        item.sender.email = jsonArrayItemSender.getString("email");
                        item.sender.emailConfirmed = jsonArrayItemSender.getBoolean("emailConfirmed");
                        item.sender.headImgFileKey = jsonArrayItemSender.getInt("headImgFileKey");
                        item.sender.id = jsonArrayItemSender.getString("id");
                        item.sender.makeEmailPublic = jsonArrayItemSender.getBoolean("makeEmailPublic");
                        item.sender.nickName = jsonArrayItemSender.getString("nickName");
                        item.sender.preferedLanguage = jsonArrayItemSender.getString("preferedLanguage");
                        item.sender.sex = jsonArrayItemSender.getString("sex");
                        item.content = jsonArrayItem.getString("content");
                        item.read = jsonArrayItem.getBoolean("read");
                        item.sendTime = jsonArrayItem.getString("sendTime");
                        r.items.add(item);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return r;
    }
}

