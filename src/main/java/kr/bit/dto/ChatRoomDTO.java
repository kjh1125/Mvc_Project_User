package kr.bit.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ChatRoomDTO {
    private int chatRoomId;
    private int profileImageId;
    private boolean manOut;
    private boolean womanOut;
    private String nickname;
    private String content;
    private String sessionStatus;
    private String endTime;
    private Integer unReadCount;
    private Timestamp lastUpdated;
}
