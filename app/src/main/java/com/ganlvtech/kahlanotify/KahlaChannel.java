package com.ganlvtech.kahlanotify;

import org.json.JSONException;

import java.io.IOException;

public class KahlaChannel {
    public static final int STATE_NEW = 0;
    public static final int STATE_LOGIN = 1;
    public static final int STATE_GET_WEBSOCKET_URL = 2;
    public static final int STATE_WEBSOCKET = 3;
    private int state;
    private String baseUrl;
    private String username;
    private String password;
    private String title;
    private KahlaWebApiClient kahlaWebApiClient;
    private KahlaWebSocketClient kahlaWebSocketClient;
    private OnLoginFailedListener onLoginFailedListener = null;
    private OnGetWebSocketUrlFailedListener onGetWebSocketUrlFailedListener = null;
    private KahlaWebSocketClient.OnOpenListener onOpenListener = null;
    private KahlaWebSocketClient.OnMessageListener onMessageListener = null;
    private KahlaWebSocketClient.OnDecryptedMessageListener onDecryptedMessageListener = null;
    private KahlaWebSocketClient.OnClosingListener onClosingListener = null;
    private KahlaWebSocketClient.OnClosedListener onClosedListener = null;
    private KahlaWebSocketClient.OnStopListener onStopListener = null;
    private KahlaWebSocketClient.OnFailureListener onFailureListener = null;

    public KahlaChannel(String baseUrl, String username, String password, String title) {
        this.baseUrl = baseUrl;
        this.username = username;
        this.password = password;
        this.title = title;
        state = STATE_NEW;
        kahlaWebApiClient = null;
        kahlaWebSocketClient = null;
    }

    public void connect() {
        try {
            kahlaWebApiClient = new KahlaWebApiClient(baseUrl);
            state = STATE_LOGIN;
            if (!kahlaWebApiClient.Login(username, password)) {
                if (onLoginFailedListener != null) {
                    onLoginFailedListener.onLoginFailed(baseUrl, username, password, title);
                }
                return;
            }
            state = STATE_GET_WEBSOCKET_URL;
            String webSocketUrl = kahlaWebApiClient.getWebSocketUrl();
            if (webSocketUrl == null) {
                if (onGetWebSocketUrlFailedListener != null) {
                    onGetWebSocketUrlFailedListener.onGetWebSocketUrlFailed(baseUrl, username, password, title);
                }
                return;
            }
            state = STATE_WEBSOCKET;
            kahlaWebSocketClient = new KahlaWebSocketClient(webSocketUrl);
            kahlaWebSocketClient.tag = title;
            kahlaWebSocketClient.setOnOpenListener(onOpenListener);
            kahlaWebSocketClient.setOnMessageListener(onMessageListener);
            kahlaWebSocketClient.setOnDecryptedMessageListener(onDecryptedMessageListener);
            kahlaWebSocketClient.setOnClosingListener(onClosingListener);
            kahlaWebSocketClient.setOnClosedListener(onClosedListener);
            kahlaWebSocketClient.setOnStopListener(onStopListener);
            kahlaWebSocketClient.setOnFailureListener(onFailureListener);
            kahlaWebSocketClient.connect();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (kahlaWebSocketClient != null) {
            kahlaWebSocketClient.stop();
        }
    }

    public String getTitle() {
        return title;
    }

    public int getState() {
        return state;
    }

    public KahlaWebApiClient getKahlaWebApiClient() {
        return kahlaWebApiClient;
    }

    public KahlaWebSocketClient getKahlaWebSocketClient() {
        return kahlaWebSocketClient;
    }

    public void setOnLoginFailedListener(OnLoginFailedListener onLoginFailedListener) {
        this.onLoginFailedListener = onLoginFailedListener;
    }

    public void setOnGetWebSocketUrlFailedListener(OnGetWebSocketUrlFailedListener onGetWebSocketUrlFailedListener) {
        this.onGetWebSocketUrlFailedListener = onGetWebSocketUrlFailedListener;
    }

    public void setOnOpenListener(KahlaWebSocketClient.OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    public void setOnMessageListener(KahlaWebSocketClient.OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    public void setOnDecryptedMessageListener(KahlaWebSocketClient.OnDecryptedMessageListener onDecryptedMessageListener) {
        this.onDecryptedMessageListener = onDecryptedMessageListener;
    }

    public void setOnClosingListener(KahlaWebSocketClient.OnClosingListener onClosingListener) {
        this.onClosingListener = onClosingListener;
    }

    public void setOnClosedListener(KahlaWebSocketClient.OnClosedListener onClosedListener) {
        this.onClosedListener = onClosedListener;
    }

    public void setOnStopListener(KahlaWebSocketClient.OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

    public void setOnFailureListener(KahlaWebSocketClient.OnFailureListener onFailureListener) {
        this.onFailureListener = onFailureListener;
    }

    public interface OnLoginFailedListener {
        void onLoginFailed(String baseUrl, String username, String password, String title);
    }

    public interface OnGetWebSocketUrlFailedListener {
        void onGetWebSocketUrlFailed(String baseUrl, String username, String password, String title);
    }
}
