package com.ganlvtech.kahlanotify.kahla.models;

import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.lib.DateParser;

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

    public Date getAccountCreateTime() {
        return DateParser.tryParse(accountCreateTime);
    }

    public String getHeadImgFileUrl(OssService ossService) {
        return ossService.getDownloadFromKeyUrl(headImgFileKey, 100, 100);
    }
}
