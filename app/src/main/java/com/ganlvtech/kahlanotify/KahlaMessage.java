package com.ganlvtech.kahlanotify;

import org.json.JSONObject;

import java.text.SimpleDateFormat;

public class KahlaMessage {
    private static final SimpleDateFormat simpleDateFormat = (SimpleDateFormat) SimpleDateFormat.getTimeInstance();
    public String title;
    public String content;
    public JSONObject jsonObject;
    public long createTime;

    public KahlaMessage(String title, String content, JSONObject jsonObject) {
        this.title = title;
        this.content = content;
        this.jsonObject = jsonObject;
        createTime = System.currentTimeMillis();
    }

    public String createTimeToString() {
        return simpleDateFormat.format(createTime);
    }

    @Override
    public String toString() {
        return createTimeToString() + " " + title + ": " + content;
    }
}
