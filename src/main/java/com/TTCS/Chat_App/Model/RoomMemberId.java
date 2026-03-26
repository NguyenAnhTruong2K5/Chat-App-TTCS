package com.TTCS.Chat_App.Model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@EqualsAndHashCode
public class RoomMemberId implements Serializable {
    private String user;
    private String room;
}
