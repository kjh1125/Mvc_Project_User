package kr.bit.dao;

import kr.bit.dto.RoomStatusDTO;
import kr.bit.entity.*;
import kr.bit.mapper.ChatMapper;
import org.apache.ibatis.annotations.Param;
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

    public void insertMessage(Message message){chatMapper.insertMessage(message);}

    public int insertChatClosure(ChatClosure chatClosure){ return chatMapper.insertChatClosure(chatClosure);}

    public Integer getClosureId(ChatClosure chatClosure){return chatMapper.getClosureId(chatClosure);}

    public RoomStatusDTO getSessionStatus(int roomId){return chatMapper.getSessionStatus(roomId);}

    public void manOut(int roomId){chatMapper.manOut(roomId);}

    public void womanOut(int roomId){chatMapper.womanOut(roomId);}

    public void report(Report report){chatMapper.report(report);}

    public void setIsReported(int roomId){chatMapper.setIsReported(roomId);}

    public String getOppositeNickname(ChatClosure chatClosure){return chatMapper.getOppositeNickname(chatClosure);}

    public void insertCard(Card card){chatMapper.insertCard(card);}

    public Card getCardByChatEndId(int chatEndId){return chatMapper.getCardByChatEndId(chatEndId);}

    public void updateCardStatus(int closureId,int cardNumber){chatMapper.updateCardStatus(closureId, cardNumber);}

    public void manContinue(int roomId){chatMapper.manContinue(roomId);}

    public void womanContinue(int roomId){chatMapper.womanContinue(roomId);}
}


