package com.ganlvtech.kahlanotify.kahla;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FriendshipService {
    public static final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSX");
    private OkHttpClient client;
    private String baseUrl;

    public FriendshipService(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public MyFriendsResponse MyFriends() throws IOException, ResponseCodeHttpUnauthorizedException {
        Request request = new Request.Builder()
                .url(baseUrl + "/friendship/MyFriends?orderByName=false")
                .build();
        Response response = client.newCall(request).execute();
        if (response.code() == 401) {
            throw new ResponseCodeHttpUnauthorizedException();
        }
        MyFriendsResponse r = new MyFriendsResponse();
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
                        Conversation item = new Conversation();
                        JSONObject jsonArrayItem = jsonArray.getJSONObject(i);
                        item.conversationId = jsonArrayItem.getInt("conversationId");
                        item.userId = jsonArrayItem.getString("userId");
                        item.displayName = jsonArrayItem.getString("displayName");
                        item.displayImageKey = jsonArrayItem.getInt("displayImageKey");
                        item.aesKey = jsonArrayItem.getString("aesKey");
                        item.latestMessage = jsonArrayItem.isNull("latestMessage") ? null : jsonArrayItem.getString("latestMessage");
                        item.latestMessageTime = jsonArrayItem.optString("latestMessageTime");
                        item.unReadAmount = jsonArrayItem.getInt("unReadAmount");
                        item.discriminator = jsonArrayItem.getString("discriminator");
                        item.muted = jsonArrayItem.getBoolean("muted");
                        item.someoneAtMe = jsonArrayItem.optBoolean("someoneAtMe", false);
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
