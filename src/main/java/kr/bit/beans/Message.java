package kr.bit.beans;

import java.sql.Timestamp;

public class Message {
    int id;
    int roomId;
    int userId;
    String messageContent;
    Timestamp createdAt;
}
