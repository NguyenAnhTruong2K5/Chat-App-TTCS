package com.TTCS.Chat_App.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_member")
@IdClass(RoomMemberId.class)
@Getter
@Setter
@NoArgsConstructor
public class RoomMember {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "room_name", length = 255)
    private String roomName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void preSave() {
        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
