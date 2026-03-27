package com.TTCS.Chat_App.Controller;

import com.TTCS.Chat_App.DTO.MessageDTO;
import com.TTCS.Chat_App.Model.Message;
import com.TTCS.Chat_App.Model.Room;
import com.TTCS.Chat_App.Model.User;
import com.TTCS.Chat_App.Repository.MessageRepo;
import com.TTCS.Chat_App.Repository.RoomRepo;
import com.TTCS.Chat_App.Repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {
    private final MessageRepo messageRepo;
    private final UserRepo userRepo;
    private final RoomRepo roomRepo;

    @MessageMapping("/chat/{roomId}/sendMessage")
    @SendTo("/topic/room/{roomId}")
    public MessageDTO sendMessage(@DestinationVariable String roomId, @Payload MessageDTO chatMessage) {
        User sender = userRepo.findById(chatMessage.getUserId()).orElse(null);
        Room room = roomRepo.findById(roomId).orElse(null);

        if (sender != null && room != null) {
            Message message = new Message();
            message.setUser(sender);
            message.setRoom(room);
            message.setContent(chatMessage.getContent());
            if (chatMessage.getBase64ImageCode() != null) {
                message.setBase64ImageCode(chatMessage.getBase64ImageCode());
            }
            messageRepo.save(message);
            chatMessage.setCreatedAt(message.getCreatedAt().toLocalTime().toString());
            return chatMessage;
        }

        throw new IllegalArgumentException("Người dùng hoặc phòng không tồn tại!");
    }
}
