package com.ganlvtech.kahlanotify.kahla.models;

import android.support.annotation.NonNull;

import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Message {
    @NonNull
    public List<At> ats;
    @NonNull
    public String content;
    public int conversationId;
    public int id;
    public boolean read;
    @NonNull
    public String sendTime;
    @NonNull
    public User sender;
    @NonNull
    public String senderId;

    public Message(JSONObject jsonObject) throws JSONException {
        conversationId = jsonObject.getInt("conversationId");
        id = jsonObject.getInt("id");
        senderId = jsonObject.getString("senderId");
        sender = new User(jsonObject.getJSONObject("sender"));
        content = jsonObject.getString("content");
        read = jsonObject.getBoolean("read");
        sendTime = jsonObject.getString("sendTime");
        ats = new ArrayList<>();
        JSONArray jsonArray = jsonObject.getJSONArray("ats");
        for (int i = 0; i < jsonArray.length(); i++) {
            ats.add(new At(jsonArray.getJSONObject(i)));
        }
    }

    public Date getSendTime() {
        return DateParser.tryParse(sendTime);
    }

    public String getContent(String aesKey) {
        try {
            byte[] bytes = CryptoJs.aesDecrypt(content, aesKey);
            return new String(bytes, "UTF-8");
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            return "Message decode error: " + e.toString();
        }
    }

    public boolean isAt(String targetUserId) {
        for (At at : ats) {
            if (at.targetUserId.equals(targetUserId)) {
                return true;
            }
        }
        return false;
    }

    public class At {
        public String targetUserId;

        public At(JSONObject jsonObject) throws JSONException {
            targetUserId = jsonObject.getString("targetUserId");
        }
    }
}
