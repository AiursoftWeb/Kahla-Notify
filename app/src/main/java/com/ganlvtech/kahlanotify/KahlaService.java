package com.ganlvtech.kahlanotify;

import android.app.Notification;
import android.app.NotificationManager;
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

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Response;
import okhttp3.WebSocket;

public class KahlaService extends Service {
    private final static AtomicInteger counter = new AtomicInteger(1);
    private IBinder binder = new ServiceBinder();
    private Handler handler = new Handler();
    private List<KahlaWebSocketClient> kahlaWebSocketClients = new ArrayList<>();
    private OnClientChangedListener onClientChangedListener = null;

    public List<KahlaWebSocketClient> getKahlaWebSocketClients() {
        return kahlaWebSocketClients;
    }

    public void setOnClientChangedListener(OnClientChangedListener onClientChangedListener) {
        this.onClientChangedListener = onClientChangedListener;
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
        Context context = this;
        NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final long[] DEFAULT_VIBRATE_PATTERN = {0, 250, 250, 250};
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(title)
                .setContentText(str)
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
        new Thread() {
            @Override
            public void run() {
                try {
                    final KahlaWebApiClient kahlaWebApiClient = new KahlaWebApiClient(baseUrl);
                    if (!kahlaWebApiClient.Login(username, password)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                toast("Login Failed", title);
                            }
                        });
                        return;
                    }
                    String webSocketUrl = kahlaWebApiClient.getWebSocketUrl();
                    if (webSocketUrl == null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                toast("Get WebSocket URL Failed", title);
                            }
                        });
                        return;
                    }
                    addKahlaWebSocketClient(webSocketUrl, title);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void addKahlaWebSocketClient(final String webSocketUrl, final String title) {
        final KahlaWebSocketClient kahlaWebSocketClient = new KahlaWebSocketClient(webSocketUrl);
        kahlaWebSocketClient.tag = title;
        kahlaWebSocketClient.setOnOpenListener(new KahlaWebSocketClient.OnOpenListener() {
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
        kahlaWebSocketClient.setOnDecryptedMessageListener(new KahlaWebSocketClient.OnDecryptedMessageListener() {
            @Override
            public void onDecryptedMessage(final String content, final String senderNickName, String senderEmail, WebSocket webSocket, String originalText) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        KahlaService.this.notify(content, senderNickName + " [" + title + "]");
                    }
                });
            }
        });
        kahlaWebSocketClient.setOnClosedListener(new KahlaWebSocketClient.OnClosedListener() {
            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                Log.d("KahlaWebSocketClient", "Closed");
                if (kahlaWebSocketClient.isAutoRetry() && kahlaWebSocketClient.getRetryCount() <= 0) {
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
        kahlaWebSocketClient.setOnFailureListener(new KahlaWebSocketClient.OnFailureListener() {
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Log.d("KahlaWebSocketClient", "Failure");
                if (kahlaWebSocketClient.isAutoRetry() && kahlaWebSocketClient.getRetryCount() <= 0) {
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
        kahlaWebSocketClient.setOnStopListener(new KahlaWebSocketClient.OnStopListener() {
            @Override
            public void onStop(WebSocket webSocket) {
                Log.d("KahlaWebSocketClient", "Stopped");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        toast("Stopped", title);
                    }
                });
                kahlaWebSocketClients.remove(kahlaWebSocketClient);
                clientChanged();
            }
        });
        kahlaWebSocketClient.connect();
        kahlaWebSocketClients.add(kahlaWebSocketClient);
        clientChanged();
    }

    private void clientChanged() {
        if (onClientChangedListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onClientChangedListener.onClientChanged(kahlaWebSocketClients);
                }
            });
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (KahlaWebSocketClient kahlaWebSocketClient : kahlaWebSocketClients) {
            stringBuilder.append(kahlaWebSocketClient.tag);
            String state;
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
            stringBuilder.append(": ");
            stringBuilder.append(state);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }

    public void stopAllKahlaWebSocketClients() {
        for (KahlaWebSocketClient kahlaWebSocketClient : kahlaWebSocketClients) {
            kahlaWebSocketClient.stop();
        }
    }

    public interface OnClientChangedListener {
        void onClientChanged(List<KahlaWebSocketClient> kahlaWebSocketClients);
    }

    public class ServiceBinder extends Binder {
        public KahlaService getService() {
            return KahlaService.this;
        }
    }
}
