package kr.bit.beans;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UserJoinBean {
    private User user;
    private UserProfile userProfile;
    private String hobbies;
    private MultipartFile photoImage;
}
