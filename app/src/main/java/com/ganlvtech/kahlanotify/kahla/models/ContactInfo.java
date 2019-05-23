package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class ContactInfo {
    public int conversationId;
    public String userId;
    public String displayName;
    public int displayImageKey;
    public String aesKey;
    public String latestMessage;
    public String latestMessageTime;
    public int unReadAmount;
    public String discriminator;
    public boolean muted;
    public boolean someoneAtMe;

    public ContactInfo(JSONObject jsonObject) throws JSONException {
        conversationId = jsonObject.getInt("conversationId");
        userId = jsonObject.getString("userId");
        displayName = jsonObject.getString("displayName");
        displayImageKey = jsonObject.getInt("displayImageKey");
        aesKey = jsonObject.getString("aesKey");
        latestMessage = jsonObject.isNull("latestMessage") ? null : jsonObject.getString("latestMessage");
        latestMessageTime = jsonObject.getString("latestMessageTime");
        unReadAmount = jsonObject.getInt("unReadAmount");
        discriminator = jsonObject.getString("discriminator");
        muted = jsonObject.getBoolean("muted");
        someoneAtMe = jsonObject.getBoolean("someoneAtMe");
    }

    public Date getLatestMessageTime() {
        return DateParser.tryParse(latestMessageTime);
    }

    public String getLatestMessageDecrypted() {
        if (latestMessage == null) {
            return "No message. Start talking now!";
        }
        try {
            byte[] bytes = CryptoJs.aesDecrypt(latestMessage, aesKey);
            return new String(bytes, "UTF-8");
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | BadPaddingException | UnsupportedEncodingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
            return "Message decode error: " + e.toString();
        }
    }

    public String getLatestMessageDecryptedSignleLine() {
        return getLatestMessageDecrypted().replaceAll("\\s", " ");
    }

    public String getDisplayImageUrl(OssService ossService) {
        return ossService.getDownloadFromKeyUrl(displayImageKey, 100, 100);
    }
}
