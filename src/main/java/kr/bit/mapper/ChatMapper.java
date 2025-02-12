package kr.bit.mapper;

import kr.bit.beans.ChatRoom;
import kr.bit.beans.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ChatMapper {
    @Select("SELECT id, participant_1_id AS participant1Id, enter_1 AS Enter1, continue_1 AS Continue1, participant_2_id AS participant2Id, enter_2 AS isEnter2, continue_2 AS Continue2, session_status AS sessionStatus, end_time AS endTime FROM chat_rooms WHERE participant_1_id = #{userId} OR participant_2_id = #{userId}")
    List<ChatRoom> getChatRoomsByUserId(int userId);

    @Select("select * from messages where room_id=#{roomId}")
    List<Message> getMessagesByRoomId(int roomId);

    @Select("select message_content from messages where room_id=#{roomId} order by created_at desc limit 1")
    String getLastMessageContentByRoomId(int roomId);

    @Select("select count(id) from messages where room_id=#{roomId} and is_read=false")
    Integer getUnReadCountByRoomId(int roomId);

    @Update("update chat_rooms set session_status=#{status} where id=#{id}")
    void updateSessionStatus(@Param("status") String status,@Param("id")int id);

    @Update("UPDATE chat_rooms " +
            "SET enter_1 = CASE WHEN participant_1_id = #{userId} THEN TRUE ELSE enter_1 END, " +
            "enter_2 = CASE WHEN participant_2_id = #{userId} THEN TRUE ELSE enter_2 END " +
            "WHERE id = #{roomId}")
    void updateIsEnter(@Param("userId") int userId, @Param("roomId") int roomId);

    @Select("select enter_1 as enter1 ,enter_2 as enter2 from chat_rooms where id=#{id}")
    ChatRoom isEnter(int id);

}
