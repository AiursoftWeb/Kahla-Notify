package com.ganlvtech.kahlanotify.kahla.responses.auth;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

public class VersionResponse extends BaseResponse {
    public String latestVersion;
    public String oldestSupportedVersion;
    public String downloadAddress;
}
