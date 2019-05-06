package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;
import com.ganlvtech.kahlanotify.util.LastAccountSharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


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
    private ArrayAdapter<String> autoCompleteTextViewServerArrayAdapter;
    private ArrayAdapter<String> autoCompleteTextViewEmailArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_login);

        autoCompleteTextViewServer = findViewById(R.id.autoCompleteTextViewServer);
        autoCompleteTextViewEmail = findViewById(R.id.autoCompleteTextViewEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        autoCompleteTextViewServer.setThreshold(1);
        autoCompleteTextViewServer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteTextViewServer.showDropDown();
                }
            }
        });
        autoCompleteTextViewServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = autoCompleteTextViewServer.getText();
                if (editable.length() == 0 || "https://".startsWith(editable.toString())) {
                    autoCompleteTextViewServer.showDropDown();
                }
            }
        });
        autoCompleteTextViewEmail.setThreshold(1);
        autoCompleteTextViewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    autoCompleteTextViewEmail.showDropDown();
                }
            }
        });
        autoCompleteTextViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoCompleteTextViewEmail.showDropDown();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        List<String> autoCompleteTextViewServerArrayList = new ArrayList<>(Arrays.asList(KAHLA_SERVER));
        autoCompleteTextViewServerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, autoCompleteTextViewServerArrayList);
        List<String> autoCompleteTextViewEmailArrayList = new ArrayList<String>();
        autoCompleteTextViewEmailArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, autoCompleteTextViewEmailArrayList);

        LastAccountSharedPreferences lastAccountSharedPreferences = new LastAccountSharedPreferences(this);
        lastAccountSharedPreferences.load();
        if (lastAccountSharedPreferences.server.length() == 0) {
            autoCompleteTextViewServer.setText("https://");
        } else {
            autoCompleteTextViewServer.setText(lastAccountSharedPreferences.server);
        }
        autoCompleteTextViewEmail.setText(lastAccountSharedPreferences.email);
        editTextPassword.setText(lastAccountSharedPreferences.password);

        AccountListSharedPreferences accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.load();
        for (AccountListSharedPreferences.Account account : accountListSharedPreferences.accountList) {
            if (!autoCompleteTextViewServerArrayList.contains(account.server)) {
                autoCompleteTextViewServerArrayAdapter.add(account.server);
            }
            if (!autoCompleteTextViewEmailArrayList.contains(account.email)) {
                autoCompleteTextViewEmailArrayAdapter.add(account.email);
            }
        }

        autoCompleteTextViewServer.setAdapter(autoCompleteTextViewServerArrayAdapter);
        autoCompleteTextViewEmail.setAdapter(autoCompleteTextViewEmailArrayAdapter);

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

    private void login() {
        String server = autoCompleteTextViewServer.getText().toString();
        String email = autoCompleteTextViewEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        LastAccountSharedPreferences lastAccountSharedPreferences = new LastAccountSharedPreferences(this);
        lastAccountSharedPreferences.server = server;
        lastAccountSharedPreferences.email = email;
        lastAccountSharedPreferences.password = password;
        lastAccountSharedPreferences.save();
        KahlaClient kahlaClient = new KahlaClient(server, email, password);
        kahlaClient.mainThreadHandler = new MyHandler(Looper.getMainLooper(), this, kahlaClient);
        kahlaClient.start();
    }

    private void loginFailed(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Sign in")
                .setMessage(message)
                .show();
    }

    private void loginOK(KahlaClient kahlaClient) {
        myService.addKahlaClient(kahlaClient);
        myService.saveConfig();
        startConversationListActivity();
    }

    private void startConversationListActivity() {
        if (!isSecondAccount) {
            Intent intent = new Intent(this, ConversationListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
        finish();
    }

    private static class MyHandler extends Handler {
        private LoginActivity loginActivity;
        private KahlaClient kahlaClient;

        MyHandler(Looper looper, LoginActivity loginActivity, KahlaClient kahlaClient) {
            super(looper);
            this.loginActivity = loginActivity;
            this.kahlaClient = kahlaClient;
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case KahlaClient.MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_RESPONSE:
                    AuthByPasswordResponse authByPasswordResponse = (AuthByPasswordResponse) msg.obj;
                    if (authByPasswordResponse.code == 0) {
                        loginActivity.loginOK(kahlaClient);
                    } else {
                        loginActivity.loginFailed(authByPasswordResponse.message);
                    }
                    break;
                case KahlaClient.MESSAGE_WHAT_AUTH_AUTH_BY_PASSWORD_EXCEPTION:
                    Exception e = (Exception) msg.obj;
                    loginActivity.loginFailed(e.toString());
                default:
                    // throw new AssertionError("Unknown handler message received: " + msg.what);
            }
        }
    }
}
