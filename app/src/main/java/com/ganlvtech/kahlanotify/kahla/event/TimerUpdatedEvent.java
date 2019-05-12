package com.ganlvtech.kahlanotify.kahla.event;

import org.json.JSONException;

public class TimerUpdatedEvent extends BaseEvent {
    public int conversationID;
    public int newTimer;

    public TimerUpdatedEvent(String json) throws JSONException {
        super(json);
        conversationID = mJsonObject.getInt("conversationID");
        newTimer = mJsonObject.getInt("newTimer");
    }
}
