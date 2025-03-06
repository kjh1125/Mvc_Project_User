package kr.bit.bean;

import kr.bit.entity.User;
import kr.bit.entity.UserProfile;
import lombok.Data;

import javax.validation.Valid;

@Data
public class UserJoinBean {
    private User user;
    @Valid
    private UserProfile userProfile;
    private String hobbies;
}
