package kr.bit.entity;

import lombok.Data;

@Data
public class User {
    private int userId;
    private String googleId;
    private String kakaoId;
    private String naverId;
    private String nickname;
}
