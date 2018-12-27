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
    public static final int STATE_NEW = 0;
    public static final int STATE_OPEN = 1;
    public static final int STATE_RETRY = 2;
    public static final int STATE_STOP = 3;
    private static OkHttpClient client;
    public String tag;
    private int state;
    private Request request;
    private WebSocket webSocket;
    private boolean autoRetry;
    private int retryInterval;
    private int retryCount;
    private OnOpenListener onOpenListener = null;
    private OnMessageListener onMessageListener = null;
    private OnDecryptedMessageListener onDecryptedMessageListener = null;
    private OnClosingListener onClosingListener = null;
    private OnClosedListener onClosedListener = null;
    private OnStopListener onStopListener = null;
    private OnFailureListener onFailureListener = null;
    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            state = STATE_OPEN;
            retryInterval = 0;
            retryCount = 0;
            if (onOpenListener != null) {
                onOpenListener.onOpen(webSocket, response);
            }
            new Thread() {
                @Override
                public void run() {
                    while (KahlaWebSocketClient.this.state == STATE_OPEN) {
                        KahlaWebSocketClient.this.webSocket.send("ping");
                        try {
                            Thread.sleep(60 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (onMessageListener != null) {
                onMessageListener.onMessage(webSocket, text);
            }
            if (onDecryptedMessageListener != null) {
                try {
                    JSONObject jsonObject = new JSONObject(text);
                    int type = jsonObject.getInt("type");
                    if (type == 0) {
                        String aesKey = jsonObject.getString("aesKey");
                        String content = jsonObject.getString("content");
                        String senderNickname = jsonObject.getJSONObject("sender").getString("nickName");
                        String senderEmail = jsonObject.getJSONObject("sender").getString("email");
                        byte[] bytes = CryptoJs.aesDecrypt(content, aesKey);
                        content = new String(bytes, "UTF-8");
                        onDecryptedMessageListener.onDecryptedMessage(content, senderNickname, senderEmail, webSocket, jsonObject);
                    }
                } catch (JSONException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (onClosingListener != null) {
                onClosingListener.onClosing(webSocket, code, reason);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            if (onClosedListener != null) {
                onClosedListener.onClosed(webSocket, code, reason);
            }
            retry(webSocket);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (onFailureListener != null) {
                onFailureListener.onFailure(webSocket, t, response);
            }
            retry(webSocket);
        }
    };

    public KahlaWebSocketClient(String url) {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        tag = url;
        state = STATE_NEW;
        autoRetry = true;
        request = new Request.Builder()
                .url(url)
                .build();
    }

    public void connect() {
        webSocket = client.newWebSocket(request, webSocketListener);
    }

    public void stop() {
        autoRetry = false;
        if (webSocket != null) {
            webSocket.cancel();
            webSocket = null;
        }
    }

    private void retry(WebSocket webSocket) {
        if (autoRetry) {
            state = STATE_RETRY;
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(retryInterval * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (retryInterval < 10) {
                        retryInterval += 1;
                    }
                    retryCount++;
                    connect();
                }
            }.start();
        } else {
            state = STATE_STOP;
            if (onStopListener != null) {
                onStopListener.onStop(webSocket);
            }
        }
    }

    public boolean isAutoRetry() {
        return autoRetry;
    }

    public int getState() {
        return state;
    }

    public int getRetryCount() {
        return retryCount;
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

    public void setOnClosingListener(OnClosingListener onClosingListener) {
        this.onClosingListener = onClosingListener;
    }

    public void setOnClosedListener(OnClosedListener onClosedListener) {
        this.onClosedListener = onClosedListener;
    }

    public void setOnFailureListener(OnFailureListener onFailureListener) {
        this.onFailureListener = onFailureListener;
    }

    public void setOnStopListener(OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
    }

    public interface OnOpenListener {
        void onOpen(WebSocket webSocket, Response response);
    }

    public interface OnMessageListener {
        void onMessage(WebSocket webSocket, String text);
    }

    public interface OnDecryptedMessageListener {
        void onDecryptedMessage(String content, String senderNickName, String senderEmail, WebSocket webSocket, JSONObject jsonObject);
    }

    public interface OnClosingListener {
        void onClosing(WebSocket webSocket, int code, String reason);
    }

    public interface OnClosedListener {
        void onClosed(WebSocket webSocket, int code, String reason);
    }

    public interface OnFailureListener {
        void onFailure(WebSocket webSocket, Throwable t, Response response);
    }

    public interface OnStopListener {
        void onStop(WebSocket webSocket);
    }
}
