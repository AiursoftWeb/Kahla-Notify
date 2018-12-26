package com.ganlvtech.kahlanotify;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Response;
import okhttp3.WebSocket;

public class KahlaService extends Service {
    private final static AtomicInteger counter = new AtomicInteger(1);
    private IBinder binder = new ServiceBinder();
    private Handler handler = new Handler();
    private List<KahlaChannel> kahlaChannels = new ArrayList<>();
    private List<KahlaMessage> kahlaMessages = new ArrayList<>();
    private OnClientChangedListener onClientChangedListener = null;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        Log.d("KAHLA", "onDestroy");
        super.onDestroy();
    }

    private void toast(String str, String title) {
        toast(title + ": " + str);
    }

    private void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    private void notify(String str, String title) {
        Context context = this;
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(str)
                .setContentIntent(pendingIntent)
                .setStyle(new Notification.BigTextStyle().bigText(str))
                .setLights(Color.CYAN, 1000, 1000)
                .setVibrate(DEFAULT_VIBRATE_PATTERN)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();
        notifyManager.notify(counter.getAndIncrement(), notification);

        final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        boolean wakeScreen = sharedPreferences.getBoolean("wakeScreen", true);
        if (wakeScreen) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!powerManager.isInteractive()) {
                PowerManager.WakeLock wl = powerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "KAHLA_NOTIFY:SCREEN_LOCK");
                wl.acquire(5000);
                PowerManager.WakeLock wl_cpu = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "KAHLA_NOTIFY:SCREEN_LOCK");
                wl_cpu.acquire(5000);
            }
        }
    }

    public void addChannel(final String baseUrl, final String username, final String password, final String title) {
        final KahlaChannel kahlaChannel = new KahlaChannel(baseUrl, username, password, title);
        kahlaChannel.setOnLoginFailedListener(new KahlaChannel.OnLoginFailedListener() {
            @Override
            public void onLoginFailed(String baseUrl, String username, String password, final String title) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast("Login Failed", title);
                    }
                });
                clientChanged();
            }
        });
        kahlaChannel.setOnGetWebSocketUrlFailedListener(new KahlaChannel.OnGetWebSocketUrlFailedListener() {
            @Override
            public void onGetWebSocketUrlFailed(String baseUrl, String username, String password, final String title) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast("Get WebSocket URL Failed", title);
                    }
                });
                clientChanged();
            }
        });
        kahlaChannel.setOnOpenListener(new KahlaWebSocketClient.OnOpenListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d("KahlaWebSocketClient", "Connected");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast("Connected", title);
                    }
                });
                clientChanged();
            }
        });
        kahlaChannel.setOnDecryptedMessageListener(new KahlaWebSocketClient.OnDecryptedMessageListener() {
            @Override
            public void onDecryptedMessage(final String content, final String senderNickName, String senderEmail, WebSocket webSocket, final JSONObject jsonObject) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String title1 = senderNickName + " [" + title + "]";
                        kahlaMessages.add(new KahlaMessage(title1, content, jsonObject));
                        KahlaService.this.notify(content, title1);
                    }
                });
            }
        });
        kahlaChannel.setOnClosedListener(new KahlaWebSocketClient.OnClosedListener() {
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("KahlaWebSocketClient", "Closed");
                if (kahlaChannel.getKahlaWebSocketClient().isAutoRetry() && kahlaChannel.getKahlaWebSocketClient().getRetryCount() <= 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            toast("Closed. Retry!", title);
                        }
                    });
                }
                clientChanged();
            }
        });
        kahlaChannel.setOnFailureListener(new KahlaWebSocketClient.OnFailureListener() {
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("KahlaWebSocketClient", "Failure");
                if (kahlaChannel.getKahlaWebSocketClient().isAutoRetry() && kahlaChannel.getKahlaWebSocketClient().getRetryCount() <= 0) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            toast("Failure. Retry!", title);
                        }
                    });
                }
                clientChanged();
            }
        });
        kahlaChannel.setOnStopListener(new KahlaWebSocketClient.OnStopListener() {
            @Override
            public void onStop(WebSocket webSocket) {
                Log.d("KahlaWebSocketClient", "Stopped");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast("Stopped", title);
                    }
                });
                kahlaChannels.remove(kahlaChannel);
                clientChanged();
            }
        });
        kahlaChannels.add(kahlaChannel);
        clientChanged();
        new Thread() {
            @Override
            public void run() {
                kahlaChannel.connect();
                clientChanged();
            }
        }.start();
    }

    private void clientChanged() {
        if (onClientChangedListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onClientChangedListener.onClientChanged();
                }
            });
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (KahlaChannel kahlaChannel : kahlaChannels) {
            stringBuilder.append(kahlaChannel.getTitle());
            String state;
            switch (kahlaChannel.getState()) {
                case KahlaChannel.STATE_NEW:
                    state = "初始化";
                    break;
                case KahlaChannel.STATE_LOGIN:
                    state = "登录中";
                    break;
                case KahlaChannel.STATE_GET_WEBSOCKET_URL:
                    state = "正在获取 WebSocket 链接";
                    break;
                case KahlaChannel.STATE_WEBSOCKET:
                    KahlaWebSocketClient kahlaWebSocketClient = kahlaChannel.getKahlaWebSocketClient();
                    switch (kahlaWebSocketClient.getState()) {
                        case KahlaWebSocketClient.STATE_NEW:
                            state = "连接中";
                            break;
                        case KahlaWebSocketClient.STATE_OPEN:
                            state = "连接成功";
                            break;
                        case KahlaWebSocketClient.STATE_RETRY:
                            state = "重连中（第 " + kahlaWebSocketClient.getRetryCount() + " 次重试）";
                            break;
                        case KahlaWebSocketClient.STATE_STOP:
                            state = "退出";
                            break;
                        default:
                            state = "Unknown state";
                    }
                    break;
                default:
                    state = "Unknown state";
            }
            stringBuilder.append(": ");
            stringBuilder.append(state);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public String messagesToString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = kahlaMessages.size() - 1; i >= 0; i--) {
            KahlaMessage kahlaMessage = kahlaMessages.get(i);
            stringBuilder.append(kahlaMessage.toString());
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void setOnClientChangedListener(OnClientChangedListener onClientChangedListener) {
        this.onClientChangedListener = onClientChangedListener;
    }

    public List<KahlaChannel> getKahlaChannels() {
        return kahlaChannels;
    }

    public List<KahlaMessage> getKahlaMessages() {
        return kahlaMessages;
    }

    public interface OnClientChangedListener {
        void onClientChanged();
    }

    public class ServiceBinder extends Binder {
        public KahlaService getService() {
            return KahlaService.this;
        }
    }
}
