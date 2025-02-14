package kr.bit.service;

import kr.bit.beans.ChatRoom;
import kr.bit.beans.Message;
import kr.bit.dao.ChatDao;
import kr.bit.dao.UserDao;
import kr.bit.dto.ChatRoomDTO;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {
    @Autowired
    private ChatDao chatDao;
    @Autowired
    private UserDao userDao;


    public List<ChatRoomDTO> getChatRoomsByUserId(int userId) {
        List<ChatRoom> chatRooms = chatDao.getChatRoomsByUserId(userId);
        if (chatRooms == null) {
            chatRooms = new ArrayList<>();
        }

        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO();
            int roomId = chatRoom.getId();
            dto.setChatRoomId(roomId);
            dto.setSessionStatus(chatRoom.getSessionStatus());
            Timestamp endTime = chatRoom.getEndTime();
            if (endTime != null) {
                ZonedDateTime zonedDateTime = endTime.toInstant()
                        .atZone(ZoneId.of("Asia/Seoul"));  // 한국 시간대로 변환

                String endTimeFormatted = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // ISO 8601 형식으로 포맷
                dto.setEndTime(endTimeFormatted);
            }

            String defaultMessage = "매칭되었습니다! 방에 참가하세요";
            switch (chatRoom.getSessionStatus()) {
                case "wait":
                    dto.setContent(defaultMessage);
                    break;
                case "on":
                case "continue":
                    String lastChat = null;
                    try {
                        lastChat = chatDao.getLastMessageContentByRoomId(roomId);
                    } catch (Exception e) {
                        lastChat = null;
                    }

                    if (lastChat != null) {
                        dto.setContent(lastChat);
                    } else {
                        dto.setContent(defaultMessage);
                    }
                    break;
                case "end":
                    dto.setContent("대화가 종료되었습니다.");
                    break;
                default:
                    break;
            }

            int otherUserId = (chatRoom.getManId() == userId) ? chatRoom.getWomanId() : chatRoom.getManId();

            // 상대방의 nickname과 profileImageId 가져오기
            String nickname = "알 수 없는 사용자";
            int profileImageId = 0;
            try {
                nickname = userDao.getNickname(otherUserId);
                profileImageId = userDao.getUserProfile(otherUserId);
            } catch (Exception e) {
                // 예외 처리: 기본값 설정
                nickname = "알 수 없는 사용자";
                profileImageId = 0;
            }

            dto.setNickname(nickname);
            dto.setProfileImageId(profileImageId);
            dto.setUnReadCount(chatDao.getUnReadCountByRoomId(roomId));
            chatRoomDTOs.add(dto);
        }

        return chatRoomDTOs;
    }



    public List<Message> getMessagesByRoomId(int roomId){
        return chatDao.getMessagesByRoomId(roomId);
    }


    public void updateIsEnter(int userId, int roomId){
        chatDao.updateIsEnter(userId, roomId);
    }

    public void isEnter(@Param("roomId") int id) {
        ChatRoom chatRoom = chatDao.isEnter(id);
        if(chatRoom.isManEnter()&&chatRoom.isWomanEnter()){
            System.out.println("시작");
            chatDao.updateSessionStatus("on",id);
        }
    }

    public void chatStart(String manId, String womanId){
        int manIdNum = Integer.parseInt(manId);
        int womanIdNum = Integer.parseInt(womanId);
        chatDao.chatStart(manIdNum,womanIdNum);
    }
}
