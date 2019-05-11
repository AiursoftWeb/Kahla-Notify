package com.ganlvtech.kahlanotify.components;

import android.content.Context;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.models.ContactInfo;

import java.util.ArrayList;

public class ContactInfoListItemAdapter extends IconTitleContentArrayAdapter {
    public OssService ossService;

    public ContactInfoListItemAdapter(Context context) {
        super(context, new ArrayList<ContactInfo>());
    }

    @Override
    public IconTitleContent getIconTitleContentItem(int position) {
        ContactInfo contactInfo = (ContactInfo) getItem(position);
        if (contactInfo != null) {
            IconTitleContent iconTitleContent = new IconTitleContent();
            iconTitleContent.placeholderResId = R.drawable.icon_default_avatar;
            if (ossService == null) {
                iconTitleContent.iconUrl = null;
            } else {
                iconTitleContent.iconUrl = contactInfo.getDisplayImageUrl(ossService);
            }
            iconTitleContent.title = contactInfo.displayName;
            iconTitleContent.content = contactInfo.getLatestMessageDecryptedSignleLine();
            iconTitleContent.unreadCount = contactInfo.unReadAmount;
            iconTitleContent.at = contactInfo.someoneAtMe;
            return iconTitleContent;
        }
        return super.getIconTitleContentItem(position);
    }
}
