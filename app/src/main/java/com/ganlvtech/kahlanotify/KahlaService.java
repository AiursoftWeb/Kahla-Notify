package com.ganlvtech.kahlanotify;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import okhttp3.Response;
import okhttp3.WebSocket;

public class KahlaService extends Service {
    public static final String CHANNEL_ID = "KAHLA_NOTIFY_CHANNEL";
    public static final long[] VIBRATION_PATTERN = {0, 250, 250, 250};
    private final static AtomicInteger counter = new AtomicInteger(1);
    private IBinder binder = new ServiceBinder();
    private Handler handler = new Handler();
    private List<KahlaChannel> kahlaChannels = new ArrayList<>();
    private List<KahlaMessage> kahlaMessages = new ArrayList<>();
    private OnClientChangedListener onClientChangedListener = null;
    private NotificationChannel notificationChannel = null;
    private NotificationManager notificationManager = null;

    @Override
    public void onCreate() {
        super.onCreate();
    }

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
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        Notification notification;
        Intent notifyIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, "Kahla Notify", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.enableLights(true);
                notificationChannel.setLightColor(Color.CYAN);
                notificationChannel.enableVibration(true);
                notificationChannel.setVibrationPattern(VIBRATION_PATTERN);
                notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI,
                        new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_ALARM)
                                .build());
                notificationManager.createNotificationChannel(notificationChannel);
            }
            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle(title)
                    .setContentText(str)
                    .setContentIntent(pendingIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(str))
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setChannelId(CHANNEL_ID)
                    .setAutoCancel(true)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(android.R.drawable.sym_def_app_icon)
                    .setContentTitle(title)
                    .setContentText(str)
                    .setContentIntent(pendingIntent)
                    .setStyle(new Notification.BigTextStyle().bigText(str))
                    .setLights(Color.CYAN, 1000, 1000)
                    .setVibrate(VIBRATION_PATTERN)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setVisibility(Notification.VISIBILITY_PUBLIC)
                    .setAutoCancel(true)
                    .build();
        }
        notificationManager.notify(counter.getAndIncrement(), notification);

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        boolean wakeScreen = sharedPreferences.getBoolean("wakeScreen", true);
        if (wakeScreen) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
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
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            JSONArray myFriends = kahlaChannel.getKahlaWebApiClient().getMyFriends();
                            for (int i = 0; i < myFriends.length(); i++) {
                                JSONObject myFriend = myFriends.getJSONObject(i);
                                int unReadAmount = myFriend.getInt("unReadAmount");
                                if (unReadAmount > 0) {
                                    String displayName = myFriend.getString("displayName");
                                    String latestMessageDecrypted = myFriend.getString("latestMessageDecrypted");
                                    final String title1 = displayName + " 的未读消息 [" + title + "]";
                                    final String content = "[" + unReadAmount + " 条] " + latestMessageDecrypted;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            KahlaService.this.notify(content, title1);
                                        }
                                    });
                                }
                            }
                        } catch (IOException | JSONException | NoSuchPaddingException | InvalidKeyException | NoSuchAlgorithmException | IllegalBlockSizeException | InvalidAlgorithmParameterException | BadPaddingException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
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
        kahlaChannel.setOnClosingListener(new KahlaWebSocketClient.OnClosingListener() {
            @Override
            public void onClosing(WebSocket webSocket, int code, String reason) {
                clientChanged();
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
        for (KahlaMessage kahlaMessage : kahlaMessages) {
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
