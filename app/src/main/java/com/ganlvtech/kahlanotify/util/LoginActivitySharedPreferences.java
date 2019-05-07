package com.ganlvtech.kahlanotify.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginActivitySharedPreferences {
    public static final String SHARED_PREFERENCES_NAME = "LastAccount";
    public String server;
    public String email;
    public String password;
    private SharedPreferences sharedPreferences;

    public LoginActivitySharedPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void load() {
        server = sharedPreferences.getString("server", "");
        email = sharedPreferences.getString("email", "");
        password = sharedPreferences.getString("password", "");
    }

    public void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("server", server);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }
}
