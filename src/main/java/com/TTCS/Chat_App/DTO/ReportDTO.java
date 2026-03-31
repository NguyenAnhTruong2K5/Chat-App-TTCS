package com.TTCS.Chat_App.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportDTO {
    private String reporterId;
    private String reportedUserId;
    private String content;
}
