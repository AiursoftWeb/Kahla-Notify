package com.ganlvtech.kahlanotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private IBinder binder = new ServiceBinder();
    private List<KahlaClient> kahlaClientList = new ArrayList<>();

    public void addKahlaClient(KahlaClient kahlaClient) {
        kahlaClientList.add(kahlaClient);
        saveConfig();
    }

    public void removeKahlaClient(KahlaClient kahlaClient) {
        kahlaClientList.remove(kahlaClient);
        saveConfig();
    }

    public List<KahlaClient> getKahlaClientList() {
        return kahlaClientList;
    }

    private void loadConfig() {
        AccountListSharedPreferences accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.load();
        for (AccountListSharedPreferences.Account account : accountListSharedPreferences.accountList) {
            KahlaClient kahlaClient = new KahlaClient(account.server, account.email, account.password);
            kahlaClientList.add(kahlaClient);
        }
    }

    private void saveConfig() {
        AccountListSharedPreferences accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.accountList.clear();
        for (KahlaClient kahlaClient : kahlaClientList) {
            AccountListSharedPreferences.Account account = new AccountListSharedPreferences.Account();
            account.server = kahlaClient.baseUrl;
            account.email = kahlaClient.email;
            account.password = kahlaClient.password;
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
        return binder;
    }

    public class ServiceBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }
}
