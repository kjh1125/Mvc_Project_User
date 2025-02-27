package kr.bit.entity;

import lombok.Data;

@Data
public class Comment {
    private int id;
    private int board_id;
    private int writer_id;
    private String content;
    private String created_at;
    private String nickname;

}
