package kr.bit.dao;

import kr.bit.entity.*;
import kr.bit.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserDao {

    @Autowired
    private UserMapper userMapper;

    public Integer googleLogin(String google_id){
        return userMapper.googleLogin(google_id);
    }

    public Integer kakaoLogin(long kakao_id){
        return userMapper.kakaoLogin(kakao_id);
    }

    public Integer naverLogin(String naver_id){
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

    public void updateUserProfile(UserProfile userprofile){
        userMapper.updateUserProfile(userprofile);
    }

    public Point getPoint(int userId){
        return userMapper.getPoint(userId);
    }

    public String getNickname(int userId){
        return userMapper.getNickname(userId);
    }

    public String getGender(int userId){
        return userMapper.getGender(userId);
    }

    public  void updateFirewood(int userId){
        userMapper.updateFirewood(userId);
    }

    public int getProfileImage(int userId){ return userMapper.getProfileImage(userId); }

    public UserProfile getProfile(int userId){ return userMapper.getProfile(userId); }

    public List<String> getUserHobbies(int userId){ return userMapper.getUserHobbies(userId); }

    public void updateReadingGlass(int userId){ userMapper.updateReadingGlass(userId); }

    public void updateProfileImage(UserProfile userProfile){ userMapper.updateProfileImage(userProfile); }
}
