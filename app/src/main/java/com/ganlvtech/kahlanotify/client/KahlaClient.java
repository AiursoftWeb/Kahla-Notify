package com.ganlvtech.kahlanotify.client;

import com.ganlvtech.kahlanotify.kahla.ApiClient;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KahlaClient extends Thread {
    public String baseUrl;
    public String email;
    public String password;
    private ApiClient apiClient;
    // private KahlaWebSocketClient kahlaWebSocketClient;
    private List<Conversation> conversationList;
    private User userInfo;

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

    // public void login() {
    //     try {
    //         apiClient.auth().AuthByPassword(email, password);
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }

    public User getUserInfo() {
        // if (userInfo == null) {
        //     try {
        //         MeResponse meResponse = apiClient.auth().Me();
        //         if (meResponse.code == 0) {
        //             userInfo = meResponse.value;
        //         }
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
        return userInfo;
    }

    public List<Conversation> getConversationList() {
        // if (conversationList == null) {
        //     try {
        //         MyFriendsResponse myFriendsResponse = apiClient.friendship().MyFriends();
        //         if (myFriendsResponse.code == 0) {
        //             conversationList = myFriendsResponse.items;
        //         }
        //     } catch (IOException e) {
        //         e.printStackTrace();
        //     }
        // }
        return conversationList;
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
            try {
                AuthByPasswordResponse authByPasswordResponse = apiClient.auth().AuthByPassword(email, password);
                if (authByPasswordResponse.code == 0) {
                    MyFriendsResponse myFriendsResponse = apiClient.friendship().MyFriends();
                    if (myFriendsResponse.code == 0) {
                        conversationList = myFriendsResponse.items;
                    }
                    MeResponse meResponse = apiClient.auth().Me();
                    if (meResponse.code == 0) {
                        userInfo = meResponse.value;
                    }
                    break;
                }
                // InitPusherResponse initPusherResponse = apiClient.auth().InitPusher();
                // kahlaWebSocketClient = new KahlaWebSocketClient(initPusherResponse.serverPath);
                // kahlaWebSocketClient.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
