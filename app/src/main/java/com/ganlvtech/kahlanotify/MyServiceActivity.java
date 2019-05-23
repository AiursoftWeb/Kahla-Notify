package com.ganlvtech.kahlanotify;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

@SuppressLint("Registered")
public class MyServiceActivity extends AppCompatActivity {
    private static final String TAG = "MyServiceActivity";
    @Nullable
    protected MyService mMyService;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            mMyService = serviceBinder.getService();
            MyServiceActivity.this.onServiceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mMyService = null;
            MyServiceActivity.this.onServiceDisconnected();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "bindService " + this);
        }
    }

    @Override
    protected void onStop() {
        unbindService(mServiceConnection);
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "unbindService " + this);
        }
        super.onStop();
    }

    protected void onServiceConnected() {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "onServiceConnected " + this);
        }
    }

    protected void onServiceDisconnected() {
        if (BuildConfig.DEBUG) {
            Log.v(TAG, "onServiceDisconnected " + this);
        }
    }
}
