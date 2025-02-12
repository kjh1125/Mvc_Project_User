package kr.bit.dto;

import lombok.Data;

import java.sql.Timestamp;
@Data
public class ChatRoomDTO {
    private int chatRoomId;
    private int profileImageId;
    private String nickname;
    private String content;
    private String sessionStatus;
    private String endTime;
    private int unReadCount;
}
