package com.ganlvtech.kahlanotify.kahla.responses.auth;

import com.ganlvtech.kahlanotify.kahla.models.User;
import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class MeResponse extends BaseResponse {
    public User value;

    public MeResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            value = new User(mJsonObject.getJSONObject("value"));
        }
    }
}
