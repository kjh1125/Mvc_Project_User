package kr.bit.mapper;

import kr.bit.entity.*;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface UserMapper {
    @Select("select user_id from users where google_id=#{googleId}")
    Integer googleLogin(String google_id);

    @Select("select user_id from users where kakao_id=#{kakaoId}")
    Integer kakaoLogin(String kakao_id);

    @Select("select user_id from users where naver_id=#{naverId}")
    Integer naverLogin(String naver_id);

    @Select("select * from hobbies")
    List<Hobby> getHobby();

    @Insert("insert into users (google_id,kakao_id,naver_id,nickname) values(#{googleId},#{kakaoId},#{naverId},#{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "userId", keyColumn = "user_id")
    void createUser(User user);

    @Insert("insert into user_profiles (user_id, gender, birth_date, photo_image_url, height, weight, religion, mbti, drinking_level, smoking_status) " +
            "values(#{userId}, #{gender}, #{birthDateString}, #{photoImageUrl}, #{height}, #{weight}, #{religion}, #{mbti}, #{drinkingLevel}, #{smokingStatus})")
    void createUserProfile(UserProfile userProfile);


    @Insert("insert into user_hobbies (user_id,hobby_id) values(${userId},${hobbyId})")
    void createUserHobby(UserHobby userHobby);

    @Update("update user_profiles set photo_image_url=#{photoImageUrl} where user_id=#{userId}")
    void updateUserProfile(UserProfile userProfile);

    @Select("select points,reading_glass as readingGlass,firewood from points where user_id=#{userId}")
    Point getPoint(int userId);

    @Select("select nickname from users where user_id=#{userId}")
    String getNickname(int userId);

    @Select("select gender from user_profiles where user_id=#{userId}")
    String getGender(int userId);

    @Update("update points set firewood=firewood-1 where user_id=#{userId}")
    void updateFirewood(int userId);

    @Update("update points set reading_glass= reading_glass-1 where user_id=#{userId}")
    void updateReadingGlass(int userId);

    @Select("select profile_image_id from user_profiles where user_id= #{userId}")
    int getProfileImage(int userId);

    @Select("select gender, birth_date as birthDate, height, weight, photo_image_url as photoImageUrl, religion, mbti, drinking_level as drinkingLevel, smoking_status as smokingStatus from user_profiles where user_id=#{userId}")
    UserProfile getProfile(int userId);

    @Select("SELECT h.name FROM hobbies h " +
            "JOIN user_hobbies uh ON h.id = uh.hobby_id " +
            "WHERE uh.user_id = #{userId}")
    List<String> getUserHobbies(int userId);

    @Update("UPDATE user_profiles set profile_image_id=${profileImageId} where user_id=#{userId}")
    void updateProfileImage(UserProfile userProfile);


    @Update("update points set points = points+ ${points} where user_id=#{userId}")
    void setPoints(Point point);

    @Update("UPDATE users SET nickname = #{value} WHERE user_id = #{userId}")
    void updateNickname(@Param("value") String value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET height = #{value} WHERE user_id = #{userId}")
    void updateHeight(@Param("value") int value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET weight = #{value} WHERE user_id = #{userId}")
    void updateWeight(@Param("value") int value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET photo_image_url = #{value} WHERE user_id = #{userId}")
    void updatePhotoImageUrl(@Param("value") String value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET religion = #{value} WHERE user_id = #{userId}")
    void updateReligion(@Param("value") String value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET mbti = #{value} WHERE user_id = #{userId}")
    void updateMbti(@Param("value") String value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET drinking_level = #{value} WHERE user_id = #{userId}")
    void updateDrinkingLevel(@Param("value") String value, @Param("userId") int userId);

    @Update("UPDATE user_profiles SET smoking_status = #{value} WHERE user_id = #{userId}")
    void updateSmokingStatus(@Param("value") String value, @Param("userId") int userId);

    @Delete("DELETE FROM user_hobbies where user_id=#{userId}")
    void deleteUserHobby(int userId);

    @Update("UPDATE points set reading_glass = reading_glass + #{readingGlass}, points = points- #{points} where user_id=#{userId}")
    void purchaseGlass(Point point);

    @Update("UPDATE points set firewood = firewood + #{firewood}, points = points- #{points} where user_id=#{userId}")
    void purchaseFirewood(Point point);

    @Delete("DELETE from users where user_id=#{userId}")
    void deleteUser(@Param("userId")int userId);
}
