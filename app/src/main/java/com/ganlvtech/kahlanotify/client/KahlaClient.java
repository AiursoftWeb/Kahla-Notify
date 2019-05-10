package com.ganlvtech.kahlanotify.client;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import com.ganlvtech.kahlanotify.kahla.ApiClient;
import com.ganlvtech.kahlanotify.kahla.exception.ResponseCodeHttpUnauthorizedException;
import com.ganlvtech.kahlanotify.kahla.models.Conversation;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.kahla.responses.auth.MeResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.GetMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.conversation.SendMessageResponse;
import com.ganlvtech.kahlanotify.kahla.responses.friendship.MyFriendsResponse;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

public class KahlaClient {
    private static final int MESSAGE_WHAT_LOGIN = 1;
    private static final int MESSAGE_WHAT_FETCH_MY_USER_INFO = MESSAGE_WHAT_LOGIN + 1;
    private static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST = MESSAGE_WHAT_FETCH_MY_USER_INFO + 1;
    private static final int MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE = MESSAGE_WHAT_FETCH_CONVERSATION_LIST + 1;
    private static final int MESSAGE_WHAT_SEND_MESSAGE = MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE + 1;

    private static final int MESSAGE_WHAT_RESPONSE = 0x1000;
    public static final int MESSAGE_WHAT_LOGIN_RESPONSE = MESSAGE_WHAT_LOGIN | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_MY_USER_INFO_RESPONSE = MESSAGE_WHAT_FETCH_MY_USER_INFO | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST_RESPONSE = MESSAGE_WHAT_FETCH_CONVERSATION_LIST | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE_RESPONSE = MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE | MESSAGE_WHAT_RESPONSE;
    public static final int MESSAGE_WHAT_SEND_MESSAGE_RESPONSE = MESSAGE_WHAT_SEND_MESSAGE | MESSAGE_WHAT_RESPONSE;

    private static final int MESSAGE_WHAT_EXCEPTION = 0x2000;
    public static final int MESSAGE_WHAT_LOGIN_EXCEPTION = MESSAGE_WHAT_LOGIN | MESSAGE_WHAT_EXCEPTION;
    public static final int MESSAGE_WHAT_FETCH_MY_USER_INFO_EXCEPTION = MESSAGE_WHAT_FETCH_MY_USER_INFO | MESSAGE_WHAT_EXCEPTION;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_LIST_EXCEPTION = MESSAGE_WHAT_FETCH_CONVERSATION_LIST | MESSAGE_WHAT_EXCEPTION;
    public static final int MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE_EXCEPTION = MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE | MESSAGE_WHAT_EXCEPTION;
    public static final int MESSAGE_WHAT_SEND_MESSAGE_EXCEPTION = MESSAGE_WHAT_SEND_MESSAGE | MESSAGE_WHAT_EXCEPTION;

    private static final String HANDLER_THREAD_NAME = "KahlaClientHandler";

    public String baseUrl;
    public String email;
    public String password;

    public ApiClient apiClient;
    @Nullable
    public List<Conversation> conversationList;
    @Nullable
    public User myUserInfo;
    @Nullable
    public Handler mainThreadHandler;
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
            sendMessageToMainThreadHandler(MESSAGE_WHAT_LOGIN_RESPONSE, authByPasswordResponse);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
            sendMessageToMainThreadHandler(MESSAGE_WHAT_LOGIN_EXCEPTION, e);
        }
    }

    private void fetchMyUserInfo(int retry) {
        try {
            MeResponse meResponse = apiClient.auth().Me();
            myUserInfo = meResponse.value;
            sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_MY_USER_INFO_RESPONSE, meResponse);
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchMyUserInfo(retry - 1);
            } else {
                e.printStackTrace();
                sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_MY_USER_INFO_EXCEPTION, e);
            }
        }
    }

    private void fetchConversationList(int retry) {
        try {
            MyFriendsResponse myFriendsResponse = apiClient.friendship().MyFriends(false);
            if (myFriendsResponse.code == 0) {
                conversationList = myFriendsResponse.items;
            }
            sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_CONVERSATION_LIST_RESPONSE, myFriendsResponse);
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchConversationList(retry - 1);
            } else {
                e.printStackTrace();
                sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_CONVERSATION_LIST_EXCEPTION, e);
            }
        }
    }

    private void fetchConversationMessage(int id, int retry) {
        try {
            GetMessageResponse getMessageResponse = apiClient.conversation().GetMessage(id);
            if (getMessageResponse.code == 0) {
                Conversation conversation = findConversationById(id);
                if (conversation != null) {
                    conversation.messageList = getMessageResponse.items;
                }
            }
            sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE_RESPONSE, getMessageResponse);
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                fetchConversationMessage(id, retry - 1);
            } else {
                e.printStackTrace();
                sendMessageToMainThreadHandler(MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE_EXCEPTION, e);
            }
        }
    }

    private void sendMessage(int id, String content, int retry) {
        try {
            SendMessageResponse sendMessageResponse = apiClient.conversation().SendMessage(id, content);
            sendMessageToMainThreadHandler(MESSAGE_WHAT_SEND_MESSAGE_RESPONSE, sendMessageResponse);
        } catch (IOException | ResponseCodeHttpUnauthorizedException e) {
            if (retry > 0) {
                login();
                sendMessage(id, content, retry - 1);
            } else {
                e.printStackTrace();
                sendMessageToMainThreadHandler(MESSAGE_WHAT_SEND_MESSAGE_EXCEPTION, e);
            }
        }
    }

    private void sendMessageToMainThreadHandler(int what, Object obj) {
        if (mainThreadHandler != null) {
            mainThreadHandler.sendMessage(mainThreadHandler.obtainMessage(what, obj));
        }
    }

    public void loginAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_LOGIN));
    }

    public void fetchMyUserInfoAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_MY_USER_INFO));
    }

    public void fetchConversationListAsync() {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION_LIST));
    }

    public void fetchConversationMessageAsync(int id) {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE, id, 0));
    }

    public void sendMessageAsync(int id, String message) {
        kahlaClientHandler.sendMessage(kahlaClientHandler.obtainMessage(MESSAGE_WHAT_SEND_MESSAGE, id, 0, message));
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
            switch (msg.what) {
                case MESSAGE_WHAT_LOGIN:
                    kahlaClient.login();
                    break;
                case MESSAGE_WHAT_FETCH_MY_USER_INFO:
                    kahlaClient.fetchMyUserInfo(1);
                    break;
                case MESSAGE_WHAT_FETCH_CONVERSATION_LIST:
                    kahlaClient.fetchConversationList(1);
                    break;
                case MESSAGE_WHAT_FETCH_CONVERSATION_MESSAGE:
                    kahlaClient.fetchConversationMessage(msg.arg1, 1);
                    break;
                case MESSAGE_WHAT_SEND_MESSAGE:
                    kahlaClient.sendMessage(msg.arg1, (String) msg.obj, 1);
                    break;
                default:
                    throw new AssertionError("Unknown message what");
            }
        }
    }
}
