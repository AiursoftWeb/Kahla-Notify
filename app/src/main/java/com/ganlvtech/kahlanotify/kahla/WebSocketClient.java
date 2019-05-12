package com.ganlvtech.kahlanotify.kahla;

import android.support.annotation.Nullable;

import com.ganlvtech.kahlanotify.kahla.event.BaseEvent;

import org.json.JSONException;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class WebSocketClient {
    public static final int WEB_SOCKET_STATE_INIT = 0;
    public static final int WEB_SOCKET_STATE_CONNECTING = WEB_SOCKET_STATE_INIT + 1;
    public static final int WEB_SOCKET_STATE_CONNECTED = WEB_SOCKET_STATE_CONNECTING + 1;
    public static final int WEB_SOCKET_STATE_CLOSING = WEB_SOCKET_STATE_CONNECTED + 1;
    public static final int WEB_SOCKET_STATE_CLOSED = WEB_SOCKET_STATE_CLOSING + 1;
    private OkHttpClient mClient;
    private String mUrl;
    private WebSocket mWebSocket;
    @Nullable
    private OnOpenListener mOnOpenListener;
    @Nullable
    private OnMessageListener mOnMessageListener;
    @Nullable
    private OnDecodedMessageListener mOnDecodedMessageListener;
    @Nullable
    private OnClosingListener mOnClosingListener;
    @Nullable
    private OnClosedListener mOnClosedListener;
    @Nullable
    private OnFailureListener mOnFailureListener;
    private boolean mAutoRetry = true;
    private int mRetryTimeout = 0;
    private int mRetryCount = 0;

    private int mState = WEB_SOCKET_STATE_INIT;

    private WebSocketListener mWebSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            mState = WEB_SOCKET_STATE_CONNECTED;
            mRetryTimeout = 0;
            mRetryCount = 0;
            if (mOnOpenListener != null) {
                mOnOpenListener.onOpen(webSocket, response);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (mOnMessageListener != null) {
                mOnMessageListener.onMessage(webSocket, text);
            }
            if (mOnDecodedMessageListener != null) {
                try {
                    BaseEvent event = BaseEvent.AutoDecode(text);
                    mOnDecodedMessageListener.onDecodedMessage(event);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            mState = WEB_SOCKET_STATE_CLOSING;
            if (mOnClosingListener != null) {
                mOnClosingListener.onClosing(webSocket, code, reason);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            mState = WEB_SOCKET_STATE_CLOSED;
            if (mOnClosedListener != null) {
                mOnClosedListener.onClosed(webSocket, code, reason);
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            mState = WEB_SOCKET_STATE_CLOSED;
            mRetryCount++;
            if (mOnFailureListener != null) {
                mOnFailureListener.onFailure(webSocket, t, response);
            }
            if (mAutoRetry) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(mRetryTimeout * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (mRetryTimeout < 45) {
                            mRetryTimeout += 1 + mRetryTimeout / 4;
                        }
                        if (mRetryTimeout > 45) {
                            mRetryTimeout = 45;
                        }
                        connect();
                    }
                }.start();
            }
        }
    };

    public WebSocketClient(String url) {
        mClient = new OkHttpClient.Builder()
                .pingInterval(45, TimeUnit.SECONDS)
                .build();
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    public WebSocket getWebSocket() {
        return mWebSocket;
    }

    public int getState() {
        return mState;
    }

    public int getRetryCount() {
        return mRetryCount;
    }

    public void connect() {
        mAutoRetry = true;
        Request request = new Request.Builder()
                .url(mUrl)
                .build();
        mWebSocket = mClient.newWebSocket(request, mWebSocketListener);
        mState = WEB_SOCKET_STATE_CONNECTING;
    }

    public void stop() {
        mState = WEB_SOCKET_STATE_CLOSING;
        mAutoRetry = false;
        mWebSocket.cancel();
    }

    public void setOnOpenListener(OnOpenListener onOpenListener) {
        mOnOpenListener = onOpenListener;
    }

    public void setOnMessageListener(OnMessageListener onMessageListener) {
        mOnMessageListener = onMessageListener;
    }

    public void setOnDecodedMessageListener(OnDecodedMessageListener onDecodedMessageListener) {
        mOnDecodedMessageListener = onDecodedMessageListener;
    }

    public void setOnClosingListener(OnClosingListener onClosingListener) {
        mOnClosingListener = onClosingListener;
    }

    public void setOnClosedListener(OnClosedListener onClosedListener) {
        mOnClosedListener = onClosedListener;
    }

    public void setOnFailureListener(OnFailureListener onFailureListener) {
        mOnFailureListener = onFailureListener;
    }

    public interface OnOpenListener {
        void onOpen(WebSocket webSocket, Response response);
    }

    public interface OnMessageListener {
        void onMessage(WebSocket webSocket, String text);
    }

    public interface OnDecodedMessageListener {
        void onDecodedMessage(BaseEvent event);
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
}
