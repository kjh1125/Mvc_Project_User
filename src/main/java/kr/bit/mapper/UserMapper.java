package kr.bit.mapper;

import kr.bit.beans.Hobby;
import kr.bit.beans.User;
import kr.bit.beans.UserHobby;
import kr.bit.beans.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserMapper {
    @Select("select user_id from users where google_id=#{googleId}")
    String googleLogin(String google_id);

    @Select("select user_id from users where google_id=#{kakaoId}")
    String kakaoLogin(long kakao_id);

    @Select("select user_id from users where google_id=#{naverId}")
    String naverLogin(String naver_id);

    @Select("select * from hobbies")
    List<Hobby> getHobby();

    @Insert("insert into users (google_id,kakao_id,naver_id,nickname) values(#{googleId},#{kakaoId},#{naverId},#{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    void createUser(User user);

    @Insert("insert into user_profiles (user_id, gender, birth_date, photo_image_url, height, weight, religion, mbti, drinking_level, smoking_status) " +
            "values(#{userId}, #{gender}, #{birthDate}, #{photoImageUrl}, #{height}, #{weight}, #{religion}, #{mbti}, #{drinkingLevel}, #{smokingStatus})")
    void createUserProfile(UserProfile userProfile);


    @Insert("insert into user_hobbies (user_id,hobby_id) values(${userId},${hobbyId})")
    void createUserHobby(UserHobby userHobby);

    @Update("update user_profiles set photo_image_url=#{photoImageUrl} where user_id=#{userId}")
    void updateUserProfile(long userId, String photoImageUrl);

}
