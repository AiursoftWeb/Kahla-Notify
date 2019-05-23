package com.ganlvtech.kahlanotify.kahla.responses.friendship;

import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;
import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MyFriendsResponse extends BaseResponse {
    public List<ContactInfo> items;

    public MyFriendsResponse(String json) throws JSONException {
        super(json);
        if (isResponseOK()) {
            items = new ArrayList<>();
            JSONArray jsonArray = mJsonObject.getJSONArray("items");
            for (int i = 0; i < jsonArray.length(); i++) {
                items.add(new ContactInfo(jsonArray.getJSONObject(i)));
            }
        }
    }
}
