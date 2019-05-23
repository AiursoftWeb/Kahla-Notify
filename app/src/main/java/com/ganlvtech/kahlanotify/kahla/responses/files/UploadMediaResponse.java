package com.ganlvtech.kahlanotify.kahla.responses.files;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class UploadMediaResponse extends BaseResponse {
    public int fileKey;
    public String downloadPath;

    public UploadMediaResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            fileKey = mJsonObject.getInt("fileKey");
            downloadPath = mJsonObject.getString("downloadPath");
        }
    }
}
