package com.ganlvtech.kahlanotify;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class KahlaWebSocketClient {
    public static final int STATE_DEFAULT = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_STOPPING = 3;
    public static final int STATE_STOPPED = 4;
    public static final int STATE_FAILURE = 5;
    public String tag;
    private String url;
    private int state = STATE_DEFAULT;
    private WebSocket webSocket;
    private OnOpenListener onOpenListener = null;
    private OnMessageListener onMessageListener = null;
    private OnDecryptedMessageListener onDecryptedMessageListener = null;
    private OnClosedListener onClosedListener = null;
    private OnFailureListener onFailureListener = null;

    public KahlaWebSocketClient(String url) {
        this.url = url;
        this.tag = url;
    }

    public int getState() {
        return state;
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }

    public void setOnOpenListener(OnOpenListener onOpenListener) {
        this.onOpenListener = onOpenListener;
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        this.onMessageListener = onMessageListener;
    }

    public void setOnDecryptedMessageListener(OnDecryptedMessageListener onDecryptedMessageListener) {
        this.onDecryptedMessageListener = onDecryptedMessageListener;
    }

    public void setOnClosedListener(OnClosedListener onClosedListener) {
        this.onClosedListener = onClosedListener;
    }

    public void setOnFailureListener(OnFailureListener onFailureListener) {
        this.onFailureListener = onFailureListener;
    }

    public WebSocket connect() {
        state = STATE_CONNECTING;
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                state = STATE_CONNECTED;
                if (onOpenListener != null) {
                    onOpenListener.onOpen(webSocket, response);
                }
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                if (onMessageListener != null) {
                    onMessageListener.onMessage(webSocket, text);
                }
                if (onDecryptedMessageListener != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(text);
                        String aesKey = jsonObject.getString("aesKey");
                        String content = jsonObject.getString("content");
                        String senderNickname = jsonObject.getJSONObject("sender").getString("nickName");
                        String senderEmail = jsonObject.getJSONObject("sender").getString("email");
                        byte[] bytes = CryptoJs.aesDecrypt(content, aesKey);
                        content = new String(bytes, "UTF-8");
                        onDecryptedMessageListener.onDecryptedMessage(content, senderNickname, senderEmail, webSocket, text);
                    } catch (JSONException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                if (onClosedListener != null) {
                    onClosedListener.onClosed(webSocket, code, reason);
                }
                state = STATE_STOPPED;
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (onFailureListener != null) {
                    onFailureListener.onFailure(webSocket, t, response);
                }
                state = STATE_FAILURE;
            }
        });
        return webSocket;
    }

    public void stop() {
        if (webSocket != null) {
            state = STATE_STOPPING;
            webSocket.cancel();
            webSocket = null;
        }
    }

    public interface OnOpenListener {
        void onOpen(WebSocket webSocket, Response response);
    }

    public interface OnMessageListener {
        void onMessage(WebSocket webSocket, String text);
    }

    public interface OnDecryptedMessageListener {
        void onDecryptedMessage(String content, String senderNickName, String senderEmail, WebSocket webSocket, String originalText);
    }

    public interface OnClosedListener {
        void onClosed(WebSocket webSocket, int code, String reason);
    }

    public interface OnFailureListener {
        void onFailure(WebSocket webSocket, Throwable t, Response response);
    }
}
