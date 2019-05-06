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
import com.ganlvtech.kahlanotify.client.KahlaClient;
import com.ganlvtech.kahlanotify.kahla.models.User;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountListItemAdapter extends ArrayAdapter {
    public AccountListItemAdapter(Context context) {
        super(context, R.layout.list_view_item_icon_title_content, new ArrayList<KahlaClient>());
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_view_item_icon_title_content, null);
        ImageView imageViewIcon = view.findViewById(R.id.imageViewIcon);
        TextView textViewTitle = view.findViewById(R.id.textViewTitle);
        TextView textViewContent = view.findViewById(R.id.textViewContent);

        KahlaClient kahlaClient = (KahlaClient) getItem(position);
        if (kahlaClient != null) {
            User user = kahlaClient.getUserInfo();
            if (user == null) {
                textViewTitle.setText(kahlaClient.email);
                textViewContent.setText(kahlaClient.baseUrl);
                Picasso.get()
                        .load(R.drawable.icon_default_avatar)
                        .into(imageViewIcon);
            } else {
                textViewTitle.setText(user.nickName);
                textViewContent.setText(kahlaClient.baseUrl);
                Picasso.get()
                        .load(user.getHeadImgFileUrl(kahlaClient.getApiClient().oss()))
                        .placeholder(R.drawable.icon_default_avatar)
                        .into(imageViewIcon);
            }
        }
        return view;
    }
}
