package com.example.mmillward89.demtalk;

import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Created by Mmillward89 on 02/08/2015.
 */
public interface GetChatCallBack {

    public abstract void done(String message, MultiUserChat multiUserChat);
}
