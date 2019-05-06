package com.ganlvtech.kahlanotify.util;

import android.content.SharedPreferences;

import com.ganlvtech.kahlanotify.client.KahlaClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private SharedPreferences sharedPreferences;

    public Storage(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public List<KahlaClient> buildKahlaClientList() {
        String userInfo = sharedPreferences.getString("UserInfo", "[]");
        List<KahlaClient> kahlaClientList = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(userInfo);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonArrayItem = jsonArray.getJSONObject(i);
                String baseUrl = jsonArrayItem.getString("baseUrl");
                String email = jsonArrayItem.getString("email");
                String password = jsonArrayItem.getString("password");
                kahlaClientList.add(new KahlaClient(baseUrl, email, password));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return kahlaClientList;
    }
}
