package com.TTCS.Chat_App.Repository;

import com.TTCS.Chat_App.DTO.MessageDTO;
import com.TTCS.Chat_App.Model.Message;
import com.TTCS.Chat_App.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepo extends JpaRepository<Message, String> {
    List<Message> findByRoom_RoomIdOrderByCreatedAtAsc(String roomId);
    Optional<Message> findTopByRoom_RoomIdOrderByCreatedAtDesc(String roomId);
}
