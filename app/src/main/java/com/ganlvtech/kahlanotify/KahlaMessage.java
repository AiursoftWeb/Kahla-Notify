package com.ganlvtech.kahlanotify;

import org.json.JSONObject;

public class KahlaMessage {
    public String title;
    public String content;
    public JSONObject jsonObject;

    public KahlaMessage(String title, String content, JSONObject jsonObject) {
        this.title = title;
        this.content = content;
        this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
        return title + ": " + content;
    }
}
