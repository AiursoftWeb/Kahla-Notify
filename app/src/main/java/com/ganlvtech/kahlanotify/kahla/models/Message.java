package com.ganlvtech.kahlanotify.kahla.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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

    @Nullable
    public static Image parseContentImage(String contentDecrypted) {
        if (contentDecrypted.startsWith("[img]")) {
            String[] parts = contentDecrypted.substring(5).split("-");
            if (parts.length > 0) {
                Image image = new Image();
                image.ossFileKey = Integer.parseInt(parts[0]);
                if (parts.length > 1) {
                    image.width = Integer.parseInt(parts[1]);
                    if (parts.length > 2) {
                        image.height = Integer.parseInt(parts[2]);
                        if (parts.length > 3) {
                            image.orientation = Integer.parseInt(parts[3]);
                        }
                    }
                }
                return image;
            }
        }
        return null;
    }

    public Date getSendTime() {
        return DateParser.tryParse(sendTime);
    }

    public String getContentDecrypted(String aesKey) {
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

    public static class Image {
        public int ossFileKey;
        public int width;
        public int height;
        public int orientation;
    }

    public class At {
        public String targetUserId;

        public At(JSONObject jsonObject) throws JSONException {
            targetUserId = jsonObject.getString("targetUserId");
        }
    }
}
