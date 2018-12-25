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
    public String tag;
    private OkHttpClient client;
    private Request request;
    private WebSocket webSocket;
    private boolean autoRetry;
    private int retryInterval;
    private OnOpenListener onOpenListener = null;
    private OnMessageListener onMessageListener = null;
    private OnDecryptedMessageListener onDecryptedMessageListener = null;
    private OnClosedListener onClosedListener = null;
    private OnStopListener onStopListener = null;
    private OnFailureListener onFailureListener = null;
    private WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            if (onOpenListener != null) {
                onOpenListener.onOpen(webSocket, response);
            }
            retryInterval = 0;
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
        tag = url;
        autoRetry = true;
        client = new OkHttpClient.Builder()
                .readTimeout(10, TimeUnit.SECONDS)
                .build();
        request = new Request.Builder()
                .url(url)
                .build();
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

    public void setOnStopListener(OnStopListener onStopListener) {
        this.onStopListener = onStopListener;
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
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(retryInterval * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    connect();
                    if (retryInterval <= 0) {
                        retryInterval += 1;
                    } else {
                        retryInterval *= 2;
                        if (retryInterval > 600) {
                            retryInterval = 600;
                        }
                    }
                }
            }.start();
        } else {
            if (onStopListener != null) {
                onStopListener.onStop(webSocket);
            }
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

    public interface OnStopListener {
        void onStop(WebSocket webSocket);
    }
}
