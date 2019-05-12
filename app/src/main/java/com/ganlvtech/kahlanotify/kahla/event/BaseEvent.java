package com.ganlvtech.kahlanotify.kahla.event;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseEvent {
    public static final int EVENT_TYPE_NEW_MESSAGE = 0;
    public static final int EVENT_TYPE_NEW_FRIEND_REQUEST = EVENT_TYPE_NEW_MESSAGE + 1;
    public static final int EVENT_TYPE_WERE_DELETED = EVENT_TYPE_NEW_FRIEND_REQUEST + 1;
    public static final int EVENT_TYPE_FRIEND_ACCEPTED = EVENT_TYPE_WERE_DELETED + 1;
    public static final int EVENT_TYPE_TIMER_UPDATED = EVENT_TYPE_FRIEND_ACCEPTED + 1;

    public int type;
    public String typeDescription;
    protected JSONObject mJsonObject;

    public BaseEvent(String json) throws JSONException {
        mJsonObject = new JSONObject(json);
        type = mJsonObject.getInt("type");
        typeDescription = mJsonObject.getString("typeDescription");
    }

    public static BaseEvent AutoDecode(String json) throws JSONException {
        BaseEvent baseEvent = new BaseEvent(json);
        switch (baseEvent.type) {
            case EVENT_TYPE_NEW_MESSAGE:
                return new NewMessageEvent(json);
            case EVENT_TYPE_NEW_FRIEND_REQUEST:
                return new NewFriendRequestEvent(json);
            case EVENT_TYPE_WERE_DELETED:
                return new WereDeletedEvent(json);
            case EVENT_TYPE_FRIEND_ACCEPTED:
                return new FriendAcceptedEvent(json);
            case EVENT_TYPE_TIMER_UPDATED:
                return new TimerUpdatedEvent(json);
            default:
                throw new AssertionError("Unknown event type");
        }
    }
}
