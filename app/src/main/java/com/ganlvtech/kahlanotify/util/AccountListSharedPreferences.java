package com.ganlvtech.kahlanotify.util;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
