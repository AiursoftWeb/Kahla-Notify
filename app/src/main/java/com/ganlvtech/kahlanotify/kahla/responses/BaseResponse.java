package com.ganlvtech.kahlanotify.kahla.responses;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseResponse {
    public static final int RESPONSE_CODE_OK = 0;

    public int code;
    public String message;
    protected JSONObject mJsonObject;

    public BaseResponse(String json) throws JSONException {
        mJsonObject = new JSONObject(json);
        code = mJsonObject.getInt("code");
        message = mJsonObject.getString("message");
    }

    public boolean isResponseOK() {
        return code == RESPONSE_CODE_OK;
    }
}
