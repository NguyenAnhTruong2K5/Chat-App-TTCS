package com.TTCS.Chat_App.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InboxMessageDTO {
    private String roomId;
    private String content;
    private String senderEmail;
    private String roomName;
}
