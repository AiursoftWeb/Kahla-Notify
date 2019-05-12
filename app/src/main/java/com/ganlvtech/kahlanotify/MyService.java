package com.ganlvtech.kahlanotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.WebSocketClient;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;
import com.ganlvtech.kahlanotify.util.Notifier;
import com.ganlvtech.kahlanotify.util.Toaster;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;
import okhttp3.WebSocket;

public class MyService extends Service {
    @NonNull
    private IBinder mBinder = new ServiceBinder();
    @NonNull
    private List<KahlaClient> mKahlaClientList = new ArrayList<>();
    private Notifier mNotifier;
    private Toaster mToaster;

    public void addKahlaClient(@NonNull final KahlaClient kahlaClient) {
        mKahlaClientList.add(kahlaClient);
        kahlaClient.getWebSocketClient().setOnOpenListener(new WebSocketClient.OnOpenListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                String account;
                if (kahlaClient.getMyUserInfo() != null) {
                    account = kahlaClient.getMyUserInfo().nickName;
                } else {
                    account = kahlaClient.getEmail();
                }
                mToaster.toast(String.format("%s (%s) connected", account, kahlaClient.getServer()));
            }
        });
        kahlaClient.getWebSocketClient().setOnFailureListener(new WebSocketClient.OnFailureListener() {
            @Override
            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                if (kahlaClient.getWebSocketClient().getRetryCount() <= 1) {
                    String account;
                    if (kahlaClient.getMyUserInfo() != null) {
                        account = kahlaClient.getMyUserInfo().nickName;
                    } else {
                        account = kahlaClient.getEmail();
                    }
                    mToaster.toast(String.format("%s (%s) disconnected", account, kahlaClient.getServer()));
                }
            }
        });
        kahlaClient.setNotifier(mNotifier);
        saveConfig();
    }

    public void removeKahlaClient(@NonNull KahlaClient kahlaClient) {
        mKahlaClientList.remove(kahlaClient);
        saveConfig();
    }

    @NonNull
    public List<KahlaClient> getKahlaClientList() {
        return mKahlaClientList;
    }

    @Nullable
    public KahlaClient getKahlaClientByServerEmail(String server, String email) {
        for (KahlaClient kahlaClient : mKahlaClientList) {
            if (server.equals(kahlaClient.getServer()) && email.equals(kahlaClient.getEmail())) {
                return kahlaClient;
            }
        }
        return null;
    }

    private void loadConfig() {
        AccountListSharedPreferences accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.load();
        for (AccountListSharedPreferences.Account account : accountListSharedPreferences.accountList) {
            addKahlaClient(new KahlaClient(account.server, account.email, account.password));
        }
    }

    private void saveConfig() {
        AccountListSharedPreferences accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.accountList.clear();
        for (KahlaClient kahlaClient : mKahlaClientList) {
            AccountListSharedPreferences.Account account = new AccountListSharedPreferences.Account();
            account.server = kahlaClient.getServer();
            account.email = kahlaClient.getEmail();
            account.password = kahlaClient.getPassword();
            accountListSharedPreferences.accountList.add(account);
        }
        accountListSharedPreferences.save();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifier = new Notifier(this);
        mToaster = new Toaster(this);
        loadConfig();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e("Kahla MyService", "onDestroy");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class ServiceBinder extends Binder {
        @NonNull
        MyService getService() {
            return MyService.this;
        }
    }
}
