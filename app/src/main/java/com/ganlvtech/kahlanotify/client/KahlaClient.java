package com.ganlvtech.kahlanotify.client;

import android.os.Handler;

import com.ganlvtech.kahlanotify.kahla.ApiClient;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KahlaClient extends Thread {
    public static final int MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_RESPONSE = 1;
    public static final int MESSAGE_WHAT_AUTH_ME_RESPONSE = 2;
    public static final int MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_RESPONSE = 3;
    public static final int MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_EXCEPTION = 1000 + MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_RESPONSE;
    public static final int MESSAGE_WHAT_AUTH_ME_EXCEPTION = 1000 + MESSAGE_WHAT_AUTH_ME_RESPONSE;
    public static final int MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_EXCEPTION = 1000 + MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_RESPONSE;
    public String baseUrl;
    public String email;
    public String password;
    public ApiClient apiClient;
    public List<Conversation> conversationList;
    public User userInfo;
    public Handler mainThreadHandler;
    public AuthByPasswordResponse authByPasswordResponse;
    public MeResponse meResponse;
    public MyFriendsResponse myFriendsResponse;

    public KahlaClient(String baseUrl, String email, String password) {
        this.baseUrl = baseUrl;
        this.email = email;
        this.password = password;
        apiClient = new ApiClient(baseUrl);
        conversationList = new ArrayList<>();
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    private void authAuthByPassword() {
        try {
            authByPasswordResponse = apiClient.auth().AuthByPassword(email, password);
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_RESPONSE, authByPasswordResponse));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_EXCEPTION, e));
        }
    }

    public void authMe() {
        try {
            meResponse = apiClient.auth().Me();
            userInfo = meResponse.value;
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_AUTH_ME_RESPONSE, meResponse));
        } catch (IOException e) {
            e.printStackTrace();
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_AUTH_ME_EXCEPTION, e));
        }
    }

    public void friendshipMyFriends() {
        try {
            myFriendsResponse = apiClient.friendship().MyFriends();
            conversationList = myFriendsResponse.items;
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_RESPONSE, myFriendsResponse));
        } catch (IOException e) {
            e.printStackTrace();
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FRIENDSHIP_GET_FRIENDS_EXCEPTION, e));
        }
    }

    // public Conversation getConversation(int conversationId) {
    //     List<Conversation> conversationList = getConversationList();
    //     if (conversationList != null) {
    //         for (Conversation conversation : conversationList) {
    //             if (conversation.conversationId == conversationId) {
    //                 if (conversation.messageList == null) {
    //                     try {
    //                         GetMessageResponse getMessageResponse = apiClient.conversation().GetMessage(conversationId);
    //                         if (getMessageResponse.code == 0) {
    //                             conversation.messageList = getMessageResponse.items;
    //                         }
    //                     } catch (IOException e) {
    //                         e.printStackTrace();
    //                     }
    //                 }
    //                 return conversation;
    //             }
    //         }
    //     }
    //     return null;
    // }

    // public void getStatus() {
    //
    // }

    // public void invalidateCache() {
    //     conversationList = null;
    // }

    @Override
    public void run() {
        while (true) {
            if (authByPasswordResponse == null || authByPasswordResponse.code != 0) {
                authAuthByPassword();
            }
            if (authByPasswordResponse != null && authByPasswordResponse.code == 0) {
                if (myFriendsResponse == null || myFriendsResponse.code != 0) {
                    friendshipMyFriends();
                }
                if (meResponse == null || meResponse.code != 0) {
                    authMe();
                }
                if (myFriendsResponse != null && myFriendsResponse.code == 0
                        && meResponse != null && meResponse.code == 0) {
                    break;
                }
            }
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    public void gracefulShutdown() {
        // TODO
    }

    public int getUnreadCount() {
        int unread = 0;
        for (Conversation conversation : conversationList) {
            unread += conversation.unReadAmount;
        }
        return unread;
    }
}
