package com.ganlvtech.kahlanotify.kahla.responses.files;

import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONException;

public class UploadFileResponse extends BaseResponse {
    public String savedFileName;
    public int fileKey;
    public int fileSize;

    public UploadFileResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            savedFileName = mJsonObject.getString("savedFileName");
            fileKey = mJsonObject.getInt("fileKey");
            fileSize = mJsonObject.getInt("fileSize");
        }
    }
}
