package com.TTCS.Chat_App.DTO;

import com.TTCS.Chat_App.Model.Room;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ChatHistoryDTO {
    private String roomName;
    private String senderEmail;
    private Room.Type type;
    private String roomId;
    private String latestMessage;
}
