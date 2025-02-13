package kr.bit.beans;

import lombok.Data;

@Data
public class Board {
    private int id;
    private int heartCount;
    private int commentCount;
    private int writerId;
    private String title;
    private String content;
    private String createdAt;
    private boolean isNotice;
    private boolean isBlind;
    private int reportCount;
    private String nickname;

    public Board(){};
}
