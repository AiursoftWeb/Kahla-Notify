package com.ganlvtech.kahlanotify;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends Activity {
    public final static String[] KAHLA_SERVER = {
            "https://server.kahla.app",
            "https://staging.server.kahla.app",
    };
    AutoCompleteTextView autoCompleteTextViewServer;
    EditText editTextEmail;
    EditText editTextPassword;
    Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        autoCompleteTextViewServer = findViewById(R.id.autoCompleteTextViewServer);
        editTextEmail = findViewById(R.id.editTextEmail);
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

        final SharedPreferences sharedPreferences = getSharedPreferences("UserInfo", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String password = sharedPreferences.getString("password", "");
        editTextEmail.setText(username);
        editTextPassword.setText(password);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startConversationListActivity();
            }
        });
    }

    private void startConversationListActivity() {
        Intent intent = new Intent(this, ConversationListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        Bundle bundle = new Bundle();
        bundle.putString("server", autoCompleteTextViewServer.getText().toString());
        bundle.putString("email", editTextEmail.getText().toString());
        bundle.putString("password", editTextPassword.getText().toString());
        intent.putExtras(bundle);
        startActivity(intent);
        this.finish();
    }
}
