package com.ganlvtech.kahlanotify.kahla.responses.conversation;

import com.ganlvtech.kahlanotify.kahla.models.Message;
import com.ganlvtech.kahlanotify.kahla.responses.BaseResponse;

import java.util.List;

public class GetMessageResponse extends BaseResponse {
    public List<Message> items;
}
