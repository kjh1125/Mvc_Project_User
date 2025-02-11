package kr.bit.dao;

import kr.bit.beans.Hobby;
import kr.bit.beans.User;
import kr.bit.beans.UserHobby;
import kr.bit.beans.UserProfile;
import kr.bit.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private UserMapper userMapper;

    public String googleLogin(String google_id){
        return userMapper.googleLogin(google_id);
    }

    public String kakaoLogin(long kakao_id){
        return userMapper.kakaoLogin(kakao_id);
    }

    public String naverLogin(String naver_id){
        return userMapper.naverLogin(naver_id);
    }

    public List<Hobby> getHobby(){
        return userMapper.getHobby();
    }

    public void createUser(User user){
        userMapper.createUser(user);
    }


    public void createUserProfile(UserProfile userProfile){
        userMapper.createUserProfile(userProfile);
    }

    public void createUserHobby(UserHobby userHobby){
        userMapper.createUserHobby(userHobby);
    }

    public void updateUserProfile(long userId, String photoImageUrl){
        userMapper.updateUserProfile(userId, photoImageUrl);
    }
}
