package kr.bit.mapper;

import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import org.apache.ibatis.annotations.*;

import java.sql.Timestamp;
import java.util.List;

public interface ChatMapper {
    @Select("SELECT id, man_id AS manId, man_enter AS manEnter, man_continue AS manContinue, woman_id AS womanId, woman_enter AS womanEnter, woman_continue AS womanContinue, session_status AS sessionStatus, end_time AS endTime FROM chat_rooms WHERE man_id = #{userId} OR woman_id = #{userId}")
    List<ChatRoom> getChatRoomsByUserId(int userId);

    @Select("select * from messages where room_id=#{roomId}")
    List<Message> getMessagesByRoomId(int roomId);

    @Select("select message_content from messages where room_id=#{roomId} order by created_at desc limit 1")
    String getLastMessageContentByRoomId(int roomId);

    @Select("select count(id) from messages where room_id=#{roomId} and is_read=false")
    Integer getUnReadCountByRoomId(int roomId);

    @Update("UPDATE chat_rooms SET man_enter = true WHERE id = #{roomId}")
    void updateManEnter(int roomId);

    @Update("UPDATE chat_rooms SET woman_enter = true WHERE id = #{roomId}")
    void updateWomanEnter(int roomId);

    @Select("SELECT (man_enter AND woman_enter) AS both_entered FROM chat_rooms WHERE id = #{roomId}")
    boolean getBothEnteredStatus(int roomId);

    @Insert("INSERT INTO chat_rooms(man_id, woman_id) VALUES(#{manId}, #{womanId})")
    @Options(useGeneratedKeys = true)
    void chatStart(ChatRoom chatRoom);

    @Update("UPDATE chat_rooms SET session_status = #{sessionStatus}, end_time = DATE_ADD(NOW(), INTERVAL #{interval} MINUTE) WHERE id = #{roomId}")
    void updateChatRoom(@Param("roomId")int roomId, @Param("sessionStatus") String sessionStatus, @Param("interval")int interval);

    @Select("select end_time from chat_rooms where id=#{roomId}")
    Timestamp getEndTime(int roomId);

    @Select("select man_id as manId, woman_id as womanId, end_time as endTime from chat_rooms where id=#{roomId}")
    ChatRoom getChatRoom(@Param("roomId")int roomId);

    @Select("select count(id) from reports where report_content=#{roomId} and report_type='room'")
    Integer getReportCount(int roomId);

    @Delete("delete from chat_rooms where id=#{roomId}")
    void deleteChatRoom(int roomId);

}
