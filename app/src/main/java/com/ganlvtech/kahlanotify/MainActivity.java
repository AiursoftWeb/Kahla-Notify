package com.ganlvtech.kahlanotify;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

public class MainActivity extends MyServiceActivity {
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MyService.class));
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onServiceConnected() {
        assert mMyService != null;
        if (mMyService.getKahlaClientList().isEmpty()) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startLoginActivity();
                }
            }, 500);
        } else {
            startConversationListActivity();
        }
    }

    private void startConversationListActivity() {
        Intent intent = new Intent(this, ConversationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }
}
