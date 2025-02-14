package kr.bit.entity;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class Message {
    private int id;
    private int roomId;
    private int userId;
    private int receiverId;
    private String messageContent;
    private Timestamp createdAt;
    private boolean read = false;
}
