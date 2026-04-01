package com.TTCS.Chat_App.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Report {
    @Id
    @Column(name = "report_id", columnDefinition = "CHAR(34)")
    private String reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id")
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @Column(name = "content", length = 255, nullable = false)
    private String content;

    @Column(name = "created_at", length = 20)
    private LocalDateTime createdAt;

    @PrePersist
    public void preSave() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }

        if (this.reportId == null) {
            this.reportId = "RE" + UUID.randomUUID().toString().replace("-", "");
        }
    }
}
