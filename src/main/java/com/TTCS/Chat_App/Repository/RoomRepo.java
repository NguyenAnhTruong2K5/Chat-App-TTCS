package com.TTCS.Chat_App.Repository;

import com.TTCS.Chat_App.Model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepo extends JpaRepository<Room, String> {
}
