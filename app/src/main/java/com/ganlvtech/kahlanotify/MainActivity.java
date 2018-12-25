package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout stopButtonsContainer;
    private Handler handler = new Handler();
    private KahlaService kahlaService = null;
    private Runnable runnableUpdateStopButtons = new Runnable() {
        @Override
        public void run() {
            stopButtonsContainer.removeAllViews();
            for (final KahlaWebSocketClient kahlaWebSocketClient : kahlaService.getKahlaWebSocketClients()) {
                Button button = new Button(MainActivity.this);
                button.setText("退出 " + kahlaWebSocketClient.tag);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        kahlaWebSocketClient.stop();
                    }
                });
                stopButtonsContainer.addView(button);
            }
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            KahlaService.ServiceBinder serviceBinder = (KahlaService.ServiceBinder) service;
            kahlaService = serviceBinder.getService();
            handler.post(runnableUpdateStopButtons);
            kahlaService.setOnClientChangedListener(new KahlaService.OnClientChangedListener() {
                @Override
                public void onClientChanged(List<KahlaWebSocketClient> kahlaWebSocketClients) {
                    handler.post(runnableUpdateStopButtons);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            kahlaService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScrollView view = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        TextView textViewUsername = new TextView(this);
        final EditText editTextUsername = new EditText(this);
        TextView textViewPassword = new TextView(this);
        final EditText editTextPassword = new EditText(this);
        final CheckBox checkBoxStaging = new CheckBox(this);
        Button buttonLogin = new Button(this);
        stopButtonsContainer = new LinearLayout(this);
        Button buttonStop = new Button(this);
        Button buttonForceExit = new Button(this);

        final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        boolean staging = sharedPreferences.getBoolean("staging", true);

        container.setOrientation(LinearLayout.VERTICAL);
        textViewUsername.setText("邮箱");
        editTextUsername.setText(username);
        textViewPassword.setText("密码");
        editTextPassword.setText(password);
        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        checkBoxStaging.setText("使用 Kahla 测试服务器");
        checkBoxStaging.setChecked(staging);
        buttonLogin.setText("登录");
        stopButtonsContainer.setOrientation(LinearLayout.VERTICAL);
        buttonStop.setText("退出全部账号");
        buttonForceExit.setText("强制退出");

        container.addView(textViewUsername);
        container.addView(editTextUsername);
        container.addView(textViewPassword);
        container.addView(editTextPassword);
        container.addView(checkBoxStaging);
        container.addView(buttonLogin);
        container.addView(stopButtonsContainer);
        container.addView(buttonStop);
        container.addView(buttonForceExit);
        view.addView(container);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = editTextUsername.getText().toString();
                final String password = editTextPassword.getText().toString();
                final boolean staging = checkBoxStaging.isChecked();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.putBoolean("staging", staging);
                editor.apply();

                String baseUrl;
                String serverEnvironment;
                if (staging) {
                    baseUrl = "https://staging.server.kahla.app";
                    serverEnvironment = "Kahla Staging";
                } else {
                    baseUrl = "https://server.kahla.app";
                    serverEnvironment = "Kahla";
                }
                String title = serverEnvironment + " | " + username;
                kahlaService.addChannel(baseUrl, username, password, title);
            }
        });
        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kahlaService.stopAllKahlaWebSocketClients();
            }
        });
        buttonForceExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.exit(0);
            }
        });

        setContentView(view);

        startService(new Intent(this, KahlaService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, KahlaService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (serviceConnection != null) {
            if (kahlaService.getKahlaWebSocketClients().size() <= 0) {
                stopService(new Intent(this, KahlaService.class));
            }
            unbindService(serviceConnection);
        }
        super.onStop();
    }
}
