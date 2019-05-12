package com.ganlvtech.kahlanotify.kahla.event;

import com.ganlvtech.kahlanotify.kahla.models.User;

import org.json.JSONException;

public class WereDeletedEvent extends BaseEvent {
    public User trigger;

    public WereDeletedEvent(String json) throws JSONException {
        super(json);
        trigger = new User(mJsonObject.getJSONObject("trigger"));
    }
}
