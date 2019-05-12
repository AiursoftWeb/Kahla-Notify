package com.ganlvtech.kahlanotify.kahla.event;

import com.ganlvtech.kahlanotify.kahla.models.User;

import org.json.JSONException;

public class FriendAcceptedEvent extends BaseEvent {
    public User target;

    public FriendAcceptedEvent(String json) throws JSONException {
        super(json);
        target = new User(mJsonObject.getJSONObject("target"));
    }
}
