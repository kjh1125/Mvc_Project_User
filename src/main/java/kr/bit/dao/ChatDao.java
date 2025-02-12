package kr.bit.dao;

import kr.bit.beans.ChatRoom;
import kr.bit.beans.Message;
import kr.bit.mapper.ChatMapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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

    public void updateSessionStatus(String status,int id){
        chatMapper.updateSessionStatus(status,id);
    }

    public void updateIsEnter(int userId, int roomId){
        chatMapper.updateIsEnter(userId, roomId);
    }

    public ChatRoom isEnter(int id){
        return chatMapper.isEnter(id);
    }
}
