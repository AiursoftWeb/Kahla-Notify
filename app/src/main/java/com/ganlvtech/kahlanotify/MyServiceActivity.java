package com.ganlvtech.kahlanotify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

@SuppressLint("Registered")
public class MyServiceActivity extends Activity {
    protected MyService myService;
    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            myService = serviceBinder.getService();
            MyServiceActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        unbindService(serviceConnection);
        super.onStop();
    }

    protected void onServiceConnected() {
    }
}
