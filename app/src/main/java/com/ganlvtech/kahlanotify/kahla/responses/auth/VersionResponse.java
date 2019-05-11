package com.ganlvtech.kahlanotify.kahla.responses.auth;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class VersionResponse extends BaseResponse {
    public String latestVersion;
    public String oldestSupportedVersion;
    public String downloadAddress;

    public VersionResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            latestVersion = mJsonObject.getString("latestVersion");
            oldestSupportedVersion = mJsonObject.getString("oldestSupportedVersion");
            downloadAddress = mJsonObject.getString("downloadAddress");
        }
    }
}
