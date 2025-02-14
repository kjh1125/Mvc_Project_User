package kr.bit.dao;

import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import kr.bit.mapper.ChatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class ChatDao {
    @Autowired
    private ChatMapper chatMapper;

    public List<ChatRoom> getChatRoomsByUserId(int userId){
        return chatMapper.getChatRoomsByUserId(userId);
    }

    public List<Message> getMessagesByRoomId(int roomId){
        return chatMapper.getMessagesByRoomId(roomId);
    }

    public String getLastMessageContentByRoomId(int roomId){
        return chatMapper.getLastMessageContentByRoomId(roomId);
    }

    public Integer getUnReadCountByRoomId(int roomId){
        return chatMapper.getUnReadCountByRoomId(roomId);
    }
    public void updateManEnter(int roomId){
        chatMapper.updateManEnter(roomId);
    }

    public void updateWomanEnter(int roomId){
        chatMapper.updateWomanEnter(roomId);
    }

    public boolean getBothEnteredStatus(int roomId){
        return chatMapper.getBothEnteredStatus(roomId);
    }

    public ChatRoom getChatRoom(int roomId){
        return chatMapper.getChatRoom(roomId);
    }

    public void chatStart(ChatRoom chatRoom){
        chatMapper.chatStart(chatRoom);
    }

    public Timestamp getEndTime(int roomId){
        return chatMapper.getEndTime(roomId);
    }

    public void updateChatRoom(int roomId, String sessionStatus, int interval){
        chatMapper.updateChatRoom(roomId, sessionStatus, interval);
    }

    public Integer getReportCount(int roomId){
        return chatMapper.getReportCount(roomId);
    }

    public void deleteChatRoom(int roomId){
        chatMapper.deleteChatRoom(roomId);
    }
}
