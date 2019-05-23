package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class FriendshipService extends BaseService {
    public FriendshipService(OkHttpClient client, String server) {
        super(client, server);
    }

    @NonNull
    public static MyFriendsResponse parseMyFriendsResponse(@NonNull Response response) throws IOException, JSONException, ResponseCodeHttpUnauthorizedException {
        mustAuthorized(response);
        assert response.body() != null;
        String json = response.body().string();
        response.close();
        return new MyFriendsResponse(json);
    }

    public Call newMyFriendsCall(boolean orderByName, int take, int skip) {
        HttpUrl url = newHttpUrlBuilder("/Friendship/MyFriends")
                .addQueryParameter("orderByName", String.valueOf(orderByName))
                .addQueryParameter("take", String.valueOf(take))
                .addQueryParameter("skip", String.valueOf(skip))
                .build();
        return newGetCall(url);
    }

    public MyFriendsResponse MyFriends(boolean orderByName, int take, int skip) throws IOException, ResponseCodeHttpUnauthorizedException, JSONException {
        return parseMyFriendsResponse(newMyFriendsCall(orderByName, take, skip).execute());
    }
}
