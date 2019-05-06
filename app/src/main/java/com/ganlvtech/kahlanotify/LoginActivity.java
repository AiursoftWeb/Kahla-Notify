package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.ganlvtech.kahlanotify.client.KahlaClient;

public class LoginActivity extends Activity {
    public final static String[] KAHLA_SERVER = {
            "https://server.kahla.app",
            "https://staging.server.kahla.app",
    };
    private AutoCompleteTextView autoCompleteTextViewServer;
    private AutoCompleteTextView autoCompleteTextViewEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private MyService myService;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyService.ServiceBinder serviceBinder = (MyService.ServiceBinder) service;
            myService = serviceBinder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };
    private boolean isSecondAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);


        autoCompleteTextViewServer = findViewById(R.id.autoCompleteTextViewServer);
        autoCompleteTextViewEmail = findViewById(R.id.autoCompleteTextViewEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, KAHLA_SERVER);
        autoCompleteTextViewServer.setAdapter(adapter);
        autoCompleteTextViewServer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteTextViewServer.showDropDown();
                }
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KahlaClient kahlaClient = new KahlaClient(autoCompleteTextViewServer.getText().toString(), autoCompleteTextViewEmail.getText().toString(), editTextPassword.getText().toString());
                myService.addKahlaClient(kahlaClient);
                kahlaClient.start();
                startConversationListActivity();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        autoCompleteTextViewEmail.setText(username);
        editTextPassword.setText(password);

        isSecondAccount = getIntent().getBooleanExtra("isSecondAccount", false);
    }

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

    private void startConversationListActivity() {
        if (!isSecondAccount) {
            Intent intent = new Intent(this, ConversationListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
        finish();
    }
}
