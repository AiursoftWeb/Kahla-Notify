package com.ganlvtech.kahlanotify.kahla.responses.auth;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class AuthByPasswordResponse extends BaseResponse {
    public AuthByPasswordResponse(String json) throws JSONException {
        super(json);
    }
}
