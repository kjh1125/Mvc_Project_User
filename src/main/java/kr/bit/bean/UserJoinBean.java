package kr.bit.bean;

import kr.bit.entity.User;
import kr.bit.entity.UserProfile;
import lombok.Data;

@Data
public class UserJoinBean {
    private User user;
    private UserProfile userProfile;
    private String hobbies;
}
