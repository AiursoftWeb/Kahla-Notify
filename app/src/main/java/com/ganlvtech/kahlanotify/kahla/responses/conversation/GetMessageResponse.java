package com.ganlvtech.kahlanotify.kahla.responses.conversation;

import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class GetMessageResponse extends BaseResponse {
    public List<Message> items;

    public GetMessageResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            items = new ArrayList<>();
            JSONArray jsonArray = mJsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                items.add(new Message(jsonArray.getJSONObject(i)));
            }
        }
    }
}
