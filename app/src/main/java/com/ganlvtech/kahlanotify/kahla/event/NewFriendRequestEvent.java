package com.ganlvtech.kahlanotify.kahla.event;

import com.ganlvtech.kahlanotify.kahla.models.User;

import org.json.JSONException;

public class NewFriendRequestEvent extends BaseEvent {
    public String requesterID;
    public User requester;

    public NewFriendRequestEvent(String json) throws JSONException {
        super(json);
        requesterID = mJsonObject.getString("requesterID");
        requester = new User(mJsonObject.getJSONObject("requester"));
    }
}
