package kr.bit.mapper;

import kr.bit.dto.RoomStatusDTO;
import kr.bit.entity.*;
import org.apache.ibatis.annotations.*;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

public interface ChatMapper {
    @Select("SELECT id, man_id AS manId, man_enter AS manEnter, man_continue AS manContinue, man_out AS manOut, " +
            "woman_id AS womanId, woman_enter AS womanEnter, woman_continue AS womanContinue, woman_out AS womanOut, " +
            "last_updated AS lastUpdated, session_status AS sessionStatus, end_time AS endTime " +
            "FROM chat_rooms " +
            "WHERE (man_id = #{userId} AND man_out = false) OR (woman_id = #{userId} AND woman_out = false) " +
            "ORDER BY last_updated DESC")
    List<ChatRoom> getChatRoomsByUserId(int userId);


    @Select("select room_id as roomId , user_id as userId, message_content as messageContent, created_at as createdAt, is_read as `read` from messages where room_id=#{roomId} ")
    List<Message> getMessagesByRoomId(@Param("roomId")int roomId);

    @Select("select id from chat_rooms where man_id=#{userId} or woman_id=#{userId};")
    List<Integer> getChatroomListByUserId(int userId);

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

    @Insert("INSERT IGNORE INTO messages (room_id,user_id,message_content,created_at,is_read) values(#{roomId},#{userId},#{messageContent},#{createdAt},#{read})")
    void insertMessage(Message message);

    @Insert("INSERT IGNORE INTO chat_closures (room_id, user_id) VALUES (#{roomId}, #{userId})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insertChatClosure(ChatClosure chatClosure);

    @Select("SELECT id from chat_closures where room_id=#{roomId} and user_id=#{userId}")
    Integer getClosureId(ChatClosure chatClosure);

    @Select("select session_status as sessionStatus, man_out as manOut, woman_out as womanOut, man_continue as manContinue, woman_continue as womanContinue, is_reported as reported from chat_rooms where id=#{roomId}")
    RoomStatusDTO getSessionStatus(int roomId);

    @Update("UPDATE chat_rooms set man_out=true where id= #{roomId}")
    void manOut(int roomId);

    @Update("UPDATE chat_rooms set woman_out=true where id= #{roomId}")
    void womanOut(int roomId);

    @Update("UPDATE chat_rooms set man_continue=true where id=#{roomId}")
    void manContinue(int roomId);

    @Update("UPDATE chat_rooms set woman_continue=true where id=#{roomId}")
    void womanContinue(int roomId);

    @Update("update  chat_rooms set is_reported = true where id=#{roomId}")
    void setIsReported(int roomId);

    @Insert("insert into reports(reporter_id,reported_id,report_type,current_id,report_content) values(#{reporterId},#{reportedId},#{reportType},#{currentId},#{reportContent})")
    void report(Report report);

    @Select("SELECT u.nickname " +
            "FROM users u " +
            "JOIN chat_rooms c " +
            "ON (u.user_id = CASE " +
            "WHEN c.man_id = #{userId} THEN c.woman_id " +
            "WHEN c.woman_id = #{userId} THEN c.man_id " +
            "ELSE NULL " +
            "END) " +
            "WHERE c.id = #{roomId} ")
    String getOppositeNickname(ChatClosure chatClosure);

    @Insert("INSERT INTO cards(chat_end_id, card_type_1,card_content_1,card_type_2,card_content_2,card_type_3,card_content_3) values(#{chatEndId},#{cardType1},#{cardContent1},#{cardType2},#{cardContent2},#{cardType3},#{cardContent3})")
    void insertCard(Card card);

    @Select("SELECT chat_end_id AS cardEndId, " +
            "card_type_1 AS cardType1, card_content_1 AS cardContent1, card_status_1 AS cardStatus1, " +
            "card_type_2 AS cardType2, card_content_2 AS cardContent2, card_status_2 AS cardStatus2, " +
            "card_type_3 AS cardType3, card_content_3 AS cardContent3, card_status_3 AS cardStatus3 " +
            "FROM cards WHERE chat_end_id = #{chatEndId}")
    Card getCardByChatEndId(int chatEndId);

    @Update("update cards set card_status_#{cardNumber}='open' where chat_end_id=#{closureId}")
    void updateCardStatus(@Param("closureId") int closureId, @Param("cardNumber") int cardNumber);

    @Update("UPDATE chat_rooms SET last_updated = NOW()  WHERE id = #{roomId}")
    void chatroomToTop(@Param("roomId")int roomId);


}
