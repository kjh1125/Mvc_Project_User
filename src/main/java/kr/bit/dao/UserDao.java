package kr.bit.dao;

import kr.bit.entity.*;
import kr.bit.mapper.UserMapper;
import org.apache.ibatis.annotations.Param;
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

    public void purchaseReadingGlass(int userId){ userMapper.updateReadingGlass(userId); }

    public void updateProfileImage(UserProfile userProfile){ userMapper.updateProfileImage(userProfile); }


    public void setPoints(Point point){userMapper.setPoints(point);}

    public void updateHeight(int height, int userId) {
        userMapper.updateHeight(height, userId);
    }

    public void updateWeight(int weight, int userId) {
        userMapper.updateWeight(weight, userId);
    }

    public void updatePhotoImageUrl(String url, int userId) {
        userMapper.updatePhotoImageUrl(url, userId);
    }

    public void updateReligion(String religion, int userId) {
        userMapper.updateReligion(religion, userId);
    }

    public void updateMbti(String mbti, int userId) {
        userMapper.updateMbti(mbti, userId);
    }

    public void updateDrinkingLevel(String drinkingLevel, int userId) {
        userMapper.updateDrinkingLevel(drinkingLevel, userId);
    }

    public void updateSmokingStatus(String smokingStatus, int userId) {
        userMapper.updateSmokingStatus(smokingStatus, userId);
    }

    public void updateNickname(@Param("value") String value, @Param("userId") int userId){
        userMapper.updateNickname(value, userId);
    }

    public void deleteUserHobby(int userId){ userMapper.deleteUserHobby(userId); }

    public void purchaseGlass(Point point){ userMapper.purchaseGlass(point); }

    public void purchaseFirewood(Point point){ userMapper.purchaseFirewood(point); }
}
