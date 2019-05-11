package com.ganlvtech.kahlanotify.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.client.KahlaClient;

import java.util.List;

public class ConversationListActivitySharedPreferences {
    public static final String SHARED_PREFERENCES_NAME = "ConversationListActivity";
    @NonNull
    public String server;
    @NonNull
    public String email;
    public String password;
    private SharedPreferences sharedPreferences;

    public ConversationListActivitySharedPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public void load() {
        server = sharedPreferences.getString("server", "");
        email = sharedPreferences.getString("email", "");
    }

    public void save() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("server", server);
        editor.putString("email", email);
        editor.apply();
    }

    public KahlaClient findKahlaClient(@NonNull List<KahlaClient> kahlaClientList) {
        if (kahlaClientList.isEmpty()) {
            return null;
        }
        for (KahlaClient kahlaClient : kahlaClientList) {
            if (server.equals(kahlaClient.getServer()) && email.equals(kahlaClient.getEmail())) {
                return kahlaClient;
            }
        }
        return null;
    }

    public void putKahlaClient(@NonNull KahlaClient kahlaClient) {
        server = kahlaClient.getServer();
        email = kahlaClient.getEmail();
    }
}
