package com.TTCS.Chat_App.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;

@Entity
@Table(name = "room")
@Getter
@Setter
@NoArgsConstructor
public class Room {
    public enum Type {
        DIRECT, GROUP
    }

    @Id
    @Column(name = "room_id", columnDefinition = "CHAR(6)")
    private String roomId;

    @Column(name = "name", length = 255)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 10)
    private Type type;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void generateId() {
        if (roomId == null) {
            this.roomId = "R" + RandomStringUtils.randomNumeric(5);
        }

        if (createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
