package com.ganlvtech.kahlanotify.components;

import android.content.Context;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.models.User;

import java.util.ArrayList;

public class AccountListItemAdapter extends IconTitleContentArrayAdapter {
    public AccountListItemAdapter(Context context) {
        super(context, new ArrayList<KahlaClient>());
    }

    @Override
    public IconTitleContent getIconTitleContentItem(int position) {
        KahlaClient kahlaClient = (KahlaClient) getItem(position);
        if (kahlaClient != null) {
            IconTitleContent iconTitleContent = new IconTitleContent();
            iconTitleContent.placeholderResId = R.drawable.icon_default_avatar;
            iconTitleContent.content = kahlaClient.baseUrl;
            User user = kahlaClient.myUserInfo;
            if (user == null) {
                iconTitleContent.title = kahlaClient.email;
                iconTitleContent.iconUrl = null;
            } else {
                iconTitleContent.title = user.nickName;
                iconTitleContent.iconUrl = user.getHeadImgFileUrl(kahlaClient.getApiClient().oss());
            }
            iconTitleContent.unreadCount = kahlaClient.getUnreadCount();
            return iconTitleContent;
        }
        return super.getIconTitleContentItem(position);
    }
}
