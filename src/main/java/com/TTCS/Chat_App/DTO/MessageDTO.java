package com.TTCS.Chat_App.DTO;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class MessageDTO {
    private String userId;
    private String roomId;
    private String userEmail;
    private String content;
    private String createdAt;
    private String base64ImageCode;
}
