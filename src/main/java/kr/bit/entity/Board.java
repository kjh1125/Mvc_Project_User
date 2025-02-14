package kr.bit.entity;

import lombok.Data;

@Data
public class Board {
    private int id;
    private int heart_count;
    private int comment_count;
    private int writer_id;
    private String title;
    private String content;
    private String created_at;
    private boolean is_notice;
    private boolean is_blind;
    private int report_count;
    private String nickname;

    public Board(){};
}
