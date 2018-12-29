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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private LinearLayout stopButtonsContainer;
    private Handler handler = new Handler();
    private KahlaService kahlaService = null;
    private TextView textViewOutput;
    private Runnable runnableUpdateStopButtons = new Runnable() {
        @Override
        public void run() {
            String output = kahlaService.toString() + "\n" + kahlaService.messagesToString();
            textViewOutput.setText(output);
            stopButtonsContainer.removeAllViews();
            for (final KahlaChannel kahlaChannel : kahlaService.getKahlaChannels()) {
                Button button = new Button(MainActivity.this);
                button.setText("退出 " + kahlaChannel.getTitle());
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        kahlaChannel.stop();
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
                public void onClientChanged() {
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
        LinearLayout linearLayoutInitialFoucs = new LinearLayout(this);
        LinearLayout container = new LinearLayout(this);
        TextView textViewUsername = new TextView(this);
        final EditText editTextUsername = new EditText(this);
        TextView textViewPassword = new TextView(this);
        final EditText editTextPassword = new EditText(this);
        final CheckBox checkBoxStaging = new CheckBox(this);
        Button buttonLogin = new Button(this);
        final CheckBox checkBoxWakeScreen = new CheckBox(this);
        stopButtonsContainer = new LinearLayout(this);
        Button buttonForceExit = new Button(this);
        textViewOutput = new TextView(this);

        final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        boolean staging = sharedPreferences.getBoolean("staging", true);
        boolean wakeScreen = sharedPreferences.getBoolean("wakeScreen", true);

        linearLayoutInitialFoucs.setFocusable(true);
        linearLayoutInitialFoucs.setFocusableInTouchMode(true);
        linearLayoutInitialFoucs.requestFocus();
        container.setOrientation(LinearLayout.VERTICAL);
        textViewUsername.setText("邮箱");
        editTextUsername.setText(username);
        textViewPassword.setText("密码");
        editTextPassword.setText(password);
        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        checkBoxStaging.setText("使用 Kahla 测试服务器");
        checkBoxStaging.setChecked(staging);
        buttonLogin.setText("登录");
        checkBoxWakeScreen.setText("通知时亮起屏幕");
        checkBoxWakeScreen.setChecked(wakeScreen);
        stopButtonsContainer.setOrientation(LinearLayout.VERTICAL);
        buttonForceExit.setText("强制退出");

        container.addView(linearLayoutInitialFoucs);
        container.addView(textViewUsername);
        container.addView(editTextUsername);
        container.addView(textViewPassword);
        container.addView(editTextPassword);
        container.addView(checkBoxStaging);
        container.addView(buttonLogin);
        container.addView(checkBoxWakeScreen);
        container.addView(stopButtonsContainer);
        container.addView(buttonForceExit);
        container.addView(textViewOutput);
        view.addView(container);

        checkBoxWakeScreen.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("wakeScreen", isChecked);
                editor.apply();
            }
        });
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
                String title = serverEnvironment;
                kahlaService.addChannel(baseUrl, username, password, title);
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
            if (kahlaService.getKahlaChannels().size() <= 0) {
                stopService(new Intent(this, KahlaService.class));
            }
            unbindService(serviceConnection);
        }
        super.onStop();
    }
}
