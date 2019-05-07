package com.ganlvtech.kahlanotify.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ganlvtech.kahlanotify.kahla.ApiClient;
import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class KahlaClient {
    private static final int MESSAGE_WHAT_RESPONSE = 0x1000;
    private static final int MESSAGE_WHAT_EXCEPTION = 0x2000;
    private static final int MESSAGE_WHAT_LOGIN = 1;
    public static final int MESSAGE_WHAT_LOGIN_RESPONSE = MESSAGE_WHAT_LOGIN | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_LOGIN_EXCEPTION = MESSAGE_WHAT_LOGIN | MESSAGE_WHAT_EXCEPTION;
    private static final int MESSAGE_WHAT_FETCH_USER_INFO = MESSAGE_WHAT_LOGIN + 1;
    public static final int MESSAGE_WHAT_FETCH_USER_INFO_RESPONSE = MESSAGE_WHAT_FETCH_USER_INFO | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_USER_INFO_EXCEPTION = MESSAGE_WHAT_FETCH_USER_INFO | MESSAGE_WHAT_EXCEPTION;
    private static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST = MESSAGE_WHAT_FETCH_USER_INFO + 1;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST_RESPONSE = MESSAGE_WHAT_FETCH_CONVERSATION_LIST | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST_EXCEPTION = MESSAGE_WHAT_FETCH_CONVERSATION_LIST | MESSAGE_WHAT_EXCEPTION;
    private static final int MESSAGE_WHAT_FETCH_CONVERSATION = MESSAGE_WHAT_FETCH_CONVERSATION_LIST + 1;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_RESPONSE = MESSAGE_WHAT_FETCH_CONVERSATION | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_EXCEPTION = MESSAGE_WHAT_FETCH_CONVERSATION | MESSAGE_WHAT_EXCEPTION;
    private static final int MESSAGE_WHAT_MESSAGE_READ = 1 + MESSAGE_WHAT_FETCH_CONVERSATION;
    private static final int MESSAGE_WHAT_SEND_MESSAGE_READ = 1 + MESSAGE_WHAT_MESSAGE_READ;

    private static final String HANDLER_THREAD_NAME = "Kahla-Client-Handler";

    public String baseUrl;
    public String email;
    public String password;

    public ApiClient apiClient;
    @Nullable
    public List<Conversation> conversationList;
    @Nullable
    public User userInfo;
    public Handler mainThreadHandler;
    private MyFriendsResponse myFriendsResponse;
    private HandlerThread handlerThread;
    private KahlaClientHandler kahlaClientHandler;

    public KahlaClient(String baseUrl, String email, String password) {
        this.baseUrl = baseUrl;
        this.email = email;
        this.password = password;
        apiClient = new ApiClient(baseUrl);
        handlerThread = new HandlerThread(HANDLER_THREAD_NAME);
        handlerThread.start();
        kahlaClientHandler = new KahlaClientHandler(handlerThread.getLooper(), this);
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    private void login() {
        try {
            AuthByPasswordResponse authByPasswordResponse = apiClient.auth().AuthByPassword(email, password);
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_LOGIN_RESPONSE, authByPasswordResponse));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_LOGIN_EXCEPTION, e));
        }
    }

    private void fetchUserInfo(int retry) {
        try {
            MeResponse meResponse = apiClient.auth().Me();
            userInfo = meResponse.value;
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_USER_INFO_RESPONSE, meResponse));
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchUserInfo(retry - 1);
            } else {
                e.printStackTrace();
                mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_USER_INFO_EXCEPTION, e));
            }
        }
    }

    private void fetchConversationList(int retry) {
        try {
            myFriendsResponse = apiClient.friendship().MyFriends();
            if (myFriendsResponse.code == 0) {
                conversationList = myFriendsResponse.items;
            }
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION_LIST_RESPONSE, myFriendsResponse));
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchConversationList(retry - 1);
            } else {
                e.printStackTrace();
                mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_USER_INFO_EXCEPTION, e));
            }
        }
    }

    private void fetchConversation(int id, int retry) {
        try {
            GetMessageResponse getMessageResponse = apiClient.conversation().GetMessage(id);
            if (getMessageResponse.code == 0) {
                Conversation conversation = findConversationById(id);
                if (conversation != null) {
                    conversation.messageList = getMessageResponse.items;
                }
            }
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION_RESPONSE, getMessageResponse));
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchConversation(id, retry - 1);
            } else {
                e.printStackTrace();
                mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(MESSAGE_WHAT_FETCH_USER_INFO_EXCEPTION, e));
            }
        }
    }

    public void loginAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_LOGIN));
    }

    public void fetchUserInfoAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_USER_INFO));
    }

    public void fetchConversationListAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION_LIST));
    }

    public void fetchConversationAsync(int id) {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION, id, 0));
    }

    public boolean quitSafely() {
        return handlerThread.quitSafely();
    }

    public int getUnreadCount() {
        if (conversationList == null) {
            return 0;
        }
        int unread = 0;
        for (Conversation conversation : conversationList) {
            unread += conversation.unReadAmount;
        }
        return unread;
    }

    public Conversation findConversationById(int id) {
        for (Conversation conversation : conversationList) {
            if (conversation.conversationId == id) {
                return conversation;
            }
        }
        return null;
    }

    class KahlaClientHandler extends Handler {
        private KahlaClient kahlaClient;

        KahlaClientHandler(Looper looper, KahlaClient kahlaClient) {
            super(looper);
            this.kahlaClient = kahlaClient;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("handleMessage", "threadName--" + Thread.currentThread().getName() + "messageWhat-" + msg.what);
            switch (msg.what) {
                case MESSAGE_WHAT_LOGIN:
                    kahlaClient.login();
                    break;
                case MESSAGE_WHAT_FETCH_USER_INFO:
                    kahlaClient.fetchUserInfo(1);
                    break;
                case MESSAGE_WHAT_FETCH_CONVERSATION_LIST:
                    kahlaClient.fetchConversationList(1);
                    break;
                case MESSAGE_WHAT_FETCH_CONVERSATION:
                    kahlaClient.fetchConversation(msg.arg1, 1);
                    break;
                case MESSAGE_WHAT_MESSAGE_READ:
                    break;
                case MESSAGE_WHAT_SEND_MESSAGE_READ:
                    break;
                default:
                    throw new AssertionError("Unknown message what");
            }
        }
    }
}
