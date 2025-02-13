package kr.bit.beans;

import lombok.Data;

@Data
public class Comment {
    private int id;
    private int boardId;
    private int writerId;
    private String content;
    private String createdAt;
    private String nickname;

    //테스트용
    public Comment(int id, int boardId, int writerId, String content, String createdAt, String nickname) {
        this.id = id;
        this.boardId = boardId;
        this.writerId = writerId;
        this.content = content;
        this.createdAt = createdAt;
        this.nickname = nickname;
    }
}
