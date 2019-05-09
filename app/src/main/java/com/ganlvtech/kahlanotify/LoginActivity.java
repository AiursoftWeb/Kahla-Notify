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
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;
import com.ganlvtech.kahlanotify.util.LoginActivitySharedPreferences;
import com.jaeger.library.StatusBarUtil;

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
    private LoginActivitySharedPreferences loginActivitySharedPreferences;
    private AccountListSharedPreferences accountListSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autoCompleteTextViewServer = findViewById(R.id.autoCompleteTextViewServer);
        autoCompleteTextViewEmail = findViewById(R.id.autoCompleteTextViewEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);

        accountListSharedPreferences = new AccountListSharedPreferences(this);
        accountListSharedPreferences.load();
        List<String> autoCompleteTextViewServerArrayList = new ArrayList<>(Arrays.asList(KAHLA_SERVER));
        List<String> autoCompleteTextViewEmailArrayList = new ArrayList<>();
        for (AccountListSharedPreferences.Account account : accountListSharedPreferences.accountList) {
            if (!autoCompleteTextViewServerArrayList.contains(account.server)) {
                autoCompleteTextViewServerArrayList.add(account.server);
            }
            if (!autoCompleteTextViewEmailArrayList.contains(account.email)) {
                autoCompleteTextViewEmailArrayList.add(account.email);
            }
        }
        autoCompleteTextViewServerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, autoCompleteTextViewServerArrayList);
        autoCompleteTextViewEmailArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, autoCompleteTextViewEmailArrayList);

        autoCompleteTextViewServer.setAdapter(autoCompleteTextViewServerArrayAdapter);
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
                autoCompleteTextViewServer.showDropDown();
            }
        });
        autoCompleteTextViewEmail.setAdapter(autoCompleteTextViewEmailArrayAdapter);
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

        loginActivitySharedPreferences = new LoginActivitySharedPreferences(this);
        loginActivitySharedPreferences.load();
        if (loginActivitySharedPreferences.server.length() == 0) {
            autoCompleteTextViewServer.setText(getString(R.string.https___));
        } else {
            autoCompleteTextViewServer.setText(loginActivitySharedPreferences.server);
        }
        autoCompleteTextViewEmail.setText(loginActivitySharedPreferences.email);
        editTextPassword.setText(loginActivitySharedPreferences.password);

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
        if (accountListSharedPreferences.isExists(server, email)) {
            loginFailed(getString(R.string.server_and_email_already_exists));
        }
        buttonLogin.setText(getString(R.string.logging_in___));
        buttonLogin.setEnabled(false);
        loginActivitySharedPreferences.server = server;
        loginActivitySharedPreferences.email = email;
        loginActivitySharedPreferences.password = password;
        loginActivitySharedPreferences.save();
        KahlaClient kahlaClient = new KahlaClient(server, email, password);
        kahlaClient.mainThreadHandler = new MyHandler(Looper.getMainLooper(), this, kahlaClient);
        kahlaClient.loginAsync();
    }

    private void loginFailed(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sign_in))
                .setMessage(message)
                .show();
        buttonLogin.setText(getString(R.string.login));
        buttonLogin.setEnabled(true);
    }

    private void loginOK(KahlaClient kahlaClient) {
        myService.addKahlaClient(kahlaClient);
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
                case KahlaClient.MESSAGE_WHAT_LOGIN_RESPONSE:
                    AuthByPasswordResponse authByPasswordResponse = (AuthByPasswordResponse) msg.obj;
                    if (authByPasswordResponse.code == 0) {
                        loginActivity.loginOK(kahlaClient);
                    } else {
                        loginActivity.loginFailed(authByPasswordResponse.message);
                    }
                    break;
                case KahlaClient.MESSAGE_WHAT_LOGIN_EXCEPTION:
                    Exception e = (Exception) msg.obj;
                    loginActivity.loginFailed(e.toString());
            }
        }
    }
}
