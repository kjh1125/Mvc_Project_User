package kr.bit.entity;

import lombok.Data;

@Data
public class UserProfile {
    private int userId;
    private String gender;
    private String birthYear;
    private String birthMonth;
    private String birthDay;
    private String createdAt;
    private Integer height;
    private Integer weight;
    private String photoImageUrl;
    private int profileImageId;
    private String religion;
    private String mbti;
    private String drinkingLevel;
    private String smokingStatus;

    public String getBirthDate() {
        return birthYear + "-" + String.format("%02d", Integer.parseInt(birthMonth)) + "-" + String.format("%02d", Integer.parseInt(birthDay));
    }

}
