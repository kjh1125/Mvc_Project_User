package kr.bit.entity;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class ChatRoom {
    private int id;                       // id -> id
    private int manId;           // participant_1_id -> participant1Id
    private boolean manEnter;             // is_enter_1 -> isEnter1
    private boolean manContinue;          // is_continue_1 -> isContinue1
    private boolean manOut;
    private int womanId;           // participant_2_id -> participant2Id
    private boolean womanEnter;             // is_enter_2 -> isEnter2
    private boolean womanContinue;          // is_continue_2 -> isContinue2
    private boolean womanOut;
    private String sessionStatus;         // session_status -> sessionStatus
    private Timestamp endTime;
    private Timestamp lastUpdated;
    private int outId;
}
