package com.ganlvtech.kahlanotify;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.responses.auth.AuthByPasswordResponse;
import com.ganlvtech.kahlanotify.util.AccountListSharedPreferences;
import com.ganlvtech.kahlanotify.util.LoginActivitySharedPreferences;

public class LoginActivity extends MyServiceActivity {
    public static final String[] DEFAULT_KAHLA_SERVER = {
            "https://server.kahla.app",
            "https://staging.server.kahla.app",
    };
    public static final String INTENT_EXTRA_NAME_IS_SECOND_ACCOUNT = "isSecondAccount";
    private AutoCompleteTextView mAutoCompleteTextViewServer;
    private AutoCompleteTextView mAutoCompleteTextViewEmail;
    private EditText mEditTextPassword;
    private Button mButtonLogin;
    private boolean mIsSecondAccount;
    private LoginActivitySharedPreferences mLoginActivitySharedPreferences;
    private AccountListSharedPreferences mAccountListSharedPreferences;
    @Nullable
    private KahlaClient mKahlaClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get intent data
        mIsSecondAccount = getIntent().getBooleanExtra(INTENT_EXTRA_NAME_IS_SECOND_ACCOUNT, false);

        // Load preferences
        mAccountListSharedPreferences = new AccountListSharedPreferences(this);
        mAccountListSharedPreferences.load();
        mLoginActivitySharedPreferences = new LoginActivitySharedPreferences(this);
        mLoginActivitySharedPreferences.load();

        // Find views
        setContentView(R.layout.activity_login);
        mAutoCompleteTextViewServer = findViewById(R.id.autoCompleteTextViewServer);
        mAutoCompleteTextViewEmail = findViewById(R.id.autoCompleteTextViewEmail);
        mEditTextPassword = findViewById(R.id.editTextPassword);
        mButtonLogin = findViewById(R.id.buttonLogin);

        // Ini Set listeners
        ArrayAdapter<String> autoCompleteTextViewServerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, mAccountListSharedPreferences.getServerList());
        mAutoCompleteTextViewServer.setAdapter(autoCompleteTextViewServerArrayAdapter);
        mAutoCompleteTextViewServer.setThreshold(1);
        mAutoCompleteTextViewServer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAutoCompleteTextViewServer.showDropDown();
                }
            }
        });
        mAutoCompleteTextViewServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoCompleteTextViewServer.showDropDown();
            }
        });

        ArrayAdapter<String> autoCompleteTextViewEmailArrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, mAccountListSharedPreferences.getEmailList());
        mAutoCompleteTextViewEmail.setAdapter(autoCompleteTextViewEmailArrayAdapter);
        mAutoCompleteTextViewEmail.setThreshold(1);
        mAutoCompleteTextViewEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mAutoCompleteTextViewEmail.showDropDown();
                }
            }
        });
        mAutoCompleteTextViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoCompleteTextViewEmail.showDropDown();
            }
        });

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        if (mLoginActivitySharedPreferences.server.length() == 0) {
            mAutoCompleteTextViewServer.setText(getString(R.string.https___));
        } else {
            mAutoCompleteTextViewServer.setText(mLoginActivitySharedPreferences.server);
        }
        mAutoCompleteTextViewEmail.setText(mLoginActivitySharedPreferences.email);
        mEditTextPassword.setText(mLoginActivitySharedPreferences.password);
    }

    private void login() {
        String server = mAutoCompleteTextViewServer.getText().toString();
        String email = mAutoCompleteTextViewEmail.getText().toString();
        String password = mEditTextPassword.getText().toString();
        if (mAccountListSharedPreferences.isExists(server, email)) {
            loginFailed(getString(R.string.server_and_email_already_exists));
            return;
        }
        mButtonLogin.setText(getString(R.string.logging_in___));
        mButtonLogin.setEnabled(false);
        mLoginActivitySharedPreferences.server = server;
        mLoginActivitySharedPreferences.email = email;
        mLoginActivitySharedPreferences.password = password;
        mLoginActivitySharedPreferences.save();
        mKahlaClient = new KahlaClient(server, email, password);
        mKahlaClient.setOnLoginResponseListener(new KahlaClient.OnLoginResponseListener() {
            @Override
            public void onLoginResponse(final AuthByPasswordResponse authByPasswordResponse, KahlaClient kahlaClient) {
                if (authByPasswordResponse.isResponseOK()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginOK();
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginFailed(authByPasswordResponse.message);
                        }
                    });
                }
            }
        });
        mKahlaClient.setOnLoginFailureListener(new KahlaClient.OnFailureListener() {
            @Override
            public void onFailure(final Exception e, KahlaClient kahlaClient) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loginFailed(e.getMessage());
                    }
                });
            }
        });
        try {
            mKahlaClient.login();
        } catch (IllegalArgumentException e) {
            loginFailed(e.getMessage());
        }
    }

    private void loginFailed(String message) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.sign_in))
                .setMessage(message)
                .show();
        mButtonLogin.setText(getString(R.string.login));
        mButtonLogin.setEnabled(true);
    }

    private void loginOK() {
        assert mMyService != null;
        if (mKahlaClient != null) {
            mMyService.addKahlaClient(mKahlaClient);
            startConversationListActivity();
        }
    }

    private void startConversationListActivity() {
        if (!mIsSecondAccount) {
            Intent intent = new Intent(this, ConversationListActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
        finish();
    }
}
