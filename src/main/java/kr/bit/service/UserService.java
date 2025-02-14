package kr.bit.service;

import kr.bit.dao.UserDao;
import kr.bit.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public  void updateFirewood(int userId){
        userDao.updateFirewood(userId);
    }

    public String getNickname(int userId){
        return userDao.getNickname(userId);
    }
}
