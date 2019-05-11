package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class User {
    public String accountCreateTime;
    public String bio;
    public String email;
    public boolean emailConfirmed;
    public boolean enableEmailNotification;
    public int headImgFileKey;
    public String id;
    public boolean makeEmailPublic;
    public String nickName;
    public String preferedLanguage;
    public String sex;
    public int themeId;

    // TODO: 2019/5/11 Rename to constructor
    public User(JSONObject jsonObject) throws JSONException {
        accountCreateTime = jsonObject.getString("accountCreateTime");
        // TODO remove null check
        bio = jsonObject.isNull("bio") ? "" : jsonObject.getString("bio");
        // TODO remove not exists check
        email = jsonObject.has("email") ? jsonObject.getString("email") : null;
        emailConfirmed = jsonObject.getBoolean("emailConfirmed");
        headImgFileKey = jsonObject.getInt("headImgFileKey");
        id = jsonObject.getString("id");
        makeEmailPublic = jsonObject.getBoolean("makeEmailPublic");
        nickName = jsonObject.getString("nickName");
        preferedLanguage = jsonObject.getString("preferedLanguage");
        sex = jsonObject.getString("sex");
    }

    public Date getAccountCreateTime() {
        return DateParser.tryParse(accountCreateTime);
    }

    public String getHeadImgFileUrl(OssService ossService) {
        return ossService.getDownloadFromKeyUrl(headImgFileKey, 100, 100);
    }
}
