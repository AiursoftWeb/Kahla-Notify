package com.ganlvtech.kahlanotify.components;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ganlvtech.kahlanotify.R;
import com.ganlvtech.kahlanotify.kahla.OssService;
import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ConversationItemAdapter extends ArrayAdapter {
    private final OssService ossService;
    private final String aesKey;

    public ConversationItemAdapter(Context context, List<Message> objects, String aesKey, OssService ossService) {
        super(context, R.layout.list_view_item_icon_title_content, objects);
        this.aesKey = aesKey;
        this.ossService = ossService;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_icon_title_content, null);
        ImageView imageViewUserAvatarIcon = view.findViewById(R.id.imageViewIcon);
        TextView textViewUserNickname = view.findViewById(R.id.textViewTitle);
        TextView textViewContent = view.findViewById(R.id.textViewContent);

        Message message = (Message) getItem(position);
        if (message != null) {
            textViewUserNickname.setText(message.sender.nickName);
            textViewContent.setText(message.getContent(aesKey).replaceAll("\\s", " "));
            Picasso.get().load(ossService.getDownloadFromKeyUrl(message.sender.headImgFileKey, 100, 100)).into(imageViewUserAvatarIcon);
        }
        return view;
    }
}
