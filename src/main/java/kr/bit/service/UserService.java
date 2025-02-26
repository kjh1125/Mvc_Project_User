package kr.bit.service;

import kr.bit.dao.UserDao;
import kr.bit.entity.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserDao userDao;

    public Integer googleLogin(String google_id){
        return userDao.googleLogin(google_id);
    }

    public Integer kakaoLogin(long kakao_id){
        return userDao.kakaoLogin(kakao_id);
    }

    public Integer naverLogin(String naver_id){
        return userDao.naverLogin(naver_id);
    }

    public List<Hobby> getHobby(){
        return userDao.getHobby();
    }

    @Transactional
    public void registerUserWithHobbies(User user, UserProfile userProfile, String hobbies) {
        // 사용자 생성 (user_id가 자동으로 생성되어 user 객체에 설정됨)
        userDao.createUser(user);

        // 생성된 user_id를 이용해 사용자 프로필 생성
        userProfile.setUserId(user.getUserId());  // userProfile에 user_id 설정
        userDao.createUserProfile(userProfile);

        // hobbies 문자열을 파싱하여 List<Integer>로 변환
        List<Integer> hobbyIds = Arrays.stream(hobbies.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        // 각 hobbyId에 대해 user_hobbies 테이블에 삽입
        // 취미 정보 저장
        for (Integer hobbyId : hobbyIds) {
            UserHobby userHobby = new UserHobby();
            userHobby.setUserId(user.getUserId());
            userHobby.setHobbyId(hobbyId);
            userDao.createUserHobby(userHobby);
        }
    }


    public Point getPoint(int userId){
        return userDao.getPoint(userId);
    }

    public String getGender(int userId){
        return userDao.getGender(userId);
    }

    public void updateFirewood(int userId){
        userDao.updateFirewood(userId);
    }

    public int getProfileImage(int userId){return userDao.getProfileImage(userId);}

    public String getNickname(int userId){return userDao.getNickname(userId);}

    public UserProfile getProfile(int userId){return userDao.getProfile(userId);}

    public void updateProfileImage(UserProfile userProfile){userDao.updateProfileImage(userProfile);}

    public void setPoints(Point point){userDao.setPoints(point);}

    public List<String> getUserHobbies(int userId){return userDao.getUserHobbies(userId);}

    public void changeUserProfile(String type, Object value, int userId) {
        switch (type) {
            case "nickname":
                userDao.updateNickname((String)value, userId);
                break;
            case "height":
                userDao.updateHeight(Integer.parseInt((String) value), userId);
                break;
            case "weight":
                userDao.updateWeight(Integer.parseInt((String) value), userId);
                break;
            case "photo_image_url":
                userDao.updatePhotoImageUrl((String) value, userId);
                break;
            case "religion":
                userDao.updateReligion((String) value, userId);
                break;
            case "mbti":
                userDao.updateMbti((String) value, userId);
                break;
            case "drinking_level":
                userDao.updateDrinkingLevel((String) value, userId);
                break;
            case "smoking_status":
                userDao.updateSmokingStatus((String) value, userId);
                break;
            default:
                throw new IllegalArgumentException("Invalid user profile type: " + type);
        }
    }

    @Transactional
    public void updateUserHobby(String hobbies, int userId){
        userDao.deleteUserHobby(userId);

        List<Integer> hobbyIds = Arrays.stream(hobbies.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        for (Integer hobbyId : hobbyIds) {
            UserHobby userHobby = new UserHobby();
            userHobby.setUserId(userId);
            userHobby.setHobbyId(hobbyId);
            userDao.createUserHobby(userHobby);
        }

    }

    public Point purchaseGlass(String value, int userId){
        Point newPoint = new Point();
        switch(value){
            case "1":{
                if(userDao.getPoint(userId).getPoints()<200){
                    return null;
                }
                newPoint.setReadingGlass(20);
                newPoint.setUserId(userId);
                newPoint.setPoints(200);
                userDao.purchaseGlass(newPoint);
                break;
            }
            case "2":{
                if(userDao.getPoint(userId).getPoints()<600){
                    return null;
                }
                newPoint.setReadingGlass(65);
                newPoint.setUserId(userId);
                newPoint.setPoints(600);
                userDao.purchaseGlass(newPoint);
                break;
            }
            case "3":{
                if(userDao.getPoint(userId).getPoints()<1200){
                    return null;
                }
                newPoint.setReadingGlass(135);
                newPoint.setUserId(userId);
                newPoint.setPoints(1200);
                userDao.purchaseGlass(newPoint);
                break;
            }
        }
        return userDao.getPoint(userId);
    }

    public Point purchaseFirewood(String value, int userId){
        Point newPoint = new Point();
        switch(value){
            case "1":{
                if(userDao.getPoint(userId).getPoints()<200){
                    return null;
                }
                newPoint.setFirewood(20);
                newPoint.setUserId(userId);
                newPoint.setPoints(200);
                userDao.purchaseFirewood(newPoint);
                break;
            }
            case "2":{
                if(userDao.getPoint(userId).getPoints()<600){
                    return null;
                }
                newPoint.setFirewood(65);
                newPoint.setUserId(userId);
                newPoint.setPoints(600);
                userDao.purchaseFirewood(newPoint);
                break;
            }
            case "3":{
                if(userDao.getPoint(userId).getPoints()<1200){
                    return null;
                }
                newPoint.setFirewood(135);
                newPoint.setUserId(userId);
                newPoint.setPoints(1200);
                userDao.purchaseFirewood(newPoint);
                break;
            }
        }
        return userDao.getPoint(userId);
    }

    public void deleteUser(@Param("userId")int userId){userDao.deleteUser(userId);}
}
