package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.CryptoJs;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Conversation {
    public String displayName;
    public int displayImageKey;
    public String latestMessage;
    public Date latestMessageTime;
    public int unReadAmount;
    public int conversationId;
    public String discriminator;
    public String userId;
    public String aesKey;
    public boolean muted;
    public boolean someoneAtMe;

    public String latestMessageDecrypted() {
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
