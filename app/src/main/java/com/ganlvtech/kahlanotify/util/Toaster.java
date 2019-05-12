package com.ganlvtech.kahlanotify.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class Toaster {
    private final Context mContext;
    private final Handler mHandler;

    public Toaster(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void toast(final String text) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
