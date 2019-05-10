package com.ganlvtech.kahlanotify.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.ganlvtech.kahlanotify.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AccountListSharedPreferences {
    public static final String SHARED_PREFERENCES_NAME = "AccountList";
    public static final String SHARED_PREFERENCES_KEY_ACCOUNT_LIST = "AccountList";
    public List<Account> accountList;
    private SharedPreferences sharedPreferences;

    public AccountListSharedPreferences(Context context) {
        this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        accountList = new ArrayList<>();
    }

    public void load() {
        accountList.clear();
        String userInfoJsonString = sharedPreferences.getString(SHARED_PREFERENCES_KEY_ACCOUNT_LIST, "[]");
        try {
            JSONArray jsonArray = new JSONArray(userInfoJsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonArrayItem = jsonArray.getJSONObject(i);
                Account userInfo = new Account();
                userInfo.fromJSONObject(jsonArrayItem);
                accountList.add(userInfo);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        JSONArray jsonArray = new JSONArray();
        for (Account account : accountList) {
            jsonArray.put(account.toJSONObject());
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SHARED_PREFERENCES_KEY_ACCOUNT_LIST, jsonArray.toString());
        editor.apply();
    }

    public boolean isExists(String server, String email) {
        for (Account account : accountList) {
            if (email.equals(account.email) && server.equals(account.server)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getServerList() {
        Set<String> stringSet = new HashSet<>(Arrays.asList(LoginActivity.DEFAULT_KAHLA_SERVER));
        for (Account account : accountList) {
            stringSet.add(account.server);
        }
        return new ArrayList<>(stringSet);
    }

    public List<String> getEmailList() {
        Set<String> stringSet = new HashSet<>();
        for (Account account : accountList) {
            stringSet.add(account.email);
        }
        return new ArrayList<>(stringSet);
    }

    public static class Account {
        public String server;
        public String email;
        public String password;

        public void fromJSONObject(JSONObject jsonObject) {
            try {
                server = jsonObject.getString("server");
                email = jsonObject.getString("email");
                password = jsonObject.getString("password");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONObject toJSONObject() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("server", server);
                jsonObject.put("email", email);
                jsonObject.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }
    }
}
