package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Conversation {
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

    public Date getLatestMessageTime() {
        return DateParser.tryParse(latestMessageTime);
    }

    public String getLatestMessage() {
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
}
