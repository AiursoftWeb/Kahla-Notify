package com.ganlvtech.kahlanotify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.ganlvtech.kahlanotify.client.KahlaClient;

import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    private IBinder binder = new ServiceBinder();
    private List<KahlaClient> kahlaClientList = new ArrayList<>();

    public void addKahlaClient(KahlaClient kahlaClient) {
        kahlaClientList.add(kahlaClient);
    }

    public List<KahlaClient> getKahlaClientList() {
        return kahlaClientList;
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
