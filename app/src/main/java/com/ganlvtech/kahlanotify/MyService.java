package com.ganlvtech.kahlanotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    @NonNull
    private IBinder mBinder = new ServiceBinder();
    @NonNull
    private List<KahlaClient> mKahlaClientList = new ArrayList<>();

    public void addKahlaClient(@NonNull KahlaClient kahlaClient) {
        mKahlaClientList.add(kahlaClient);
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
