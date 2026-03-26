package com.TTCS.Chat_App.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

@Entity
@Table(name = "user")
@Getter
@Setter
@NoArgsConstructor
public class User {
    public enum Role {
        ADMIN,
        USER
    }

    public enum Status {
        ALLOWED, BANNED
    }

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(6)")
    private String userId;

    @Column(name = "username", length = 60)
    private String username;

    @Column(name = "password", length = 60)
    private String password;

    @Column(name = "email", length = 45, unique = true)
    private String email;

    @Column(name = "bio", length = 300)
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10)
    private Status status;

    @PrePersist
    public void prePersist() {
        if (this.userId == null) {
            this.userId = "U" + RandomStringUtils.randomNumeric(5);
        }

        if (this.role == null) {
            this.role = Role.USER;
        }

        if (this.status == null) {
            this.status = Status.ALLOWED;
        }
    }
}
