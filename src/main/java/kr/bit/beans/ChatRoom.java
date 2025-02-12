package kr.bit.beans;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class ChatRoom {
    private int id;                       // id -> id
    private int participant1Id;           // participant_1_id -> participant1Id
    private boolean isEnter1;             // is_enter_1 -> isEnter1
    private boolean isContinue1;          // is_continue_1 -> isContinue1
    private int participant2Id;           // participant_2_id -> participant2Id
    private boolean isEnter2;             // is_enter_2 -> isEnter2
    private boolean isContinue2;          // is_continue_2 -> isContinue2
    private String sessionStatus;         // session_status -> sessionStatus
    private Timestamp endTime;
}
