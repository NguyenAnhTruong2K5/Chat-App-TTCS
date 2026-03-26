package com.TTCS.Chat_App.Repository;

import com.TTCS.Chat_App.Model.Room;
import com.TTCS.Chat_App.Model.RoomMember;
import com.TTCS.Chat_App.Model.RoomMemberId;
import com.TTCS.Chat_App.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomMemberRepo extends JpaRepository<RoomMember, RoomMemberId> {
    @Query("Select rm1.room From RoomMember rm1 Join RoomMember rm2 on rm1.room = rm2.room " +
            "Where rm1.room.type = :type " +
            "And rm1.user = :user1 " +
            "And rm2.user = :user2")
    Optional<Room> findExistingRoom(
            @Param("user1") User user1,
            @Param("user2") User user2,
            @Param("type") Room.Type type
    );

    List<RoomMember> findByUser(User user);
    Optional<RoomMember> findByUserAndRoom(User user, Room room);
    List<RoomMember> findByRoom(Room room);
}
