package com.ganlvtech.kahlanotify.kahla.event;

import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.models.User;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class NewMessageEvent extends BaseEvent {
    public String aesKey;
    public String content;
    public int conversationId;
    public boolean mentioned;
    public boolean muted;
    public User sender;

    public NewMessageEvent(String json) throws JSONException {
        super(json);
        aesKey = mJsonObject.getString("aesKey");
        content = mJsonObject.getString("content");
        conversationId = mJsonObject.getInt("conversationId");
        mentioned = mJsonObject.getBoolean("mentioned");
        muted = mJsonObject.getBoolean("muted");
        sender = new User(mJsonObject.getJSONObject("sender"));
    }

    public String getContentDecrypted() {
        try {
            byte[] bytes = CryptoJs.aesDecrypt(content, aesKey);
            return new String(bytes, "UTF-8");
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            return "Message decode error: " + e.toString();
        }
    }
}
