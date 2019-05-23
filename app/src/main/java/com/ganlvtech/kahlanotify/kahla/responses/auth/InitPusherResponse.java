package com.ganlvtech.kahlanotify.kahla.responses.auth;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class InitPusherResponse extends BaseResponse {
    public int channelId;
    public String connectKey;
    public String serverPath;

    public InitPusherResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            channelId = mJsonObject.getInt("channelId");
            connectKey = mJsonObject.getString("connectKey");
            serverPath = mJsonObject.getString("serverPath");
        }
    }
}
