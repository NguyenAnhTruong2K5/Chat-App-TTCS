package com.TTCS.Chat_App.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {
    private String userId;
    private String roomId;
    private String userEmail;
    private String content;
    private String createdAt;
}
