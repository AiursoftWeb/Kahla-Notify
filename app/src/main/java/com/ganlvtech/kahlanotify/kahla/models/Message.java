package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.lib.CryptoJs;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Message {
    public List<User> ats;
    public String content;
    public int conversationId;
    public int id;
    public boolean read;
    public String sendTime;
    public User sender;
    public String senderId;
    public Conversation conversation;

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
}
