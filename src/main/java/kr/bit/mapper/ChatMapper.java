package kr.bit.mapper;

import kr.bit.beans.ChatRoom;
import kr.bit.beans.Message;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ChatMapper {
    @Select("SELECT id, participant_1_id AS participant1Id, is_enter_1 AS isEnter1, is_continue_1 AS isContinue1, participant_2_id AS participant2Id, is_enter_2 AS isEnter2, is_continue_2 AS isContinue2, session_status AS sessionStatus, end_time AS endTime FROM chat_rooms WHERE participant_1_id = #{userId} OR participant_2_id = #{userId}")
    List<ChatRoom> getChatRoomsByUserId(int userId);

    @Select("select * from messages where room_id=#{roomId}")
    List<Message> getMessagesByRoomId(int roomId);

    @Select("select message_content from messages where room_id=#{roomId} order by created_at desc limit 1")
    String getLastMessageContentByRoomId(int roomId);

    @Select("select count(id) from messages where room_id=#{roomId} and is_read=false")
    int getUnReadCountByRoomId(int roomId);

}
