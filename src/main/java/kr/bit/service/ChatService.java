package kr.bit.service;

import kr.bit.dto.RoomStatusDTO;
import kr.bit.entity.ChatClosure;
import kr.bit.entity.ChatRoom;
import kr.bit.dao.ChatDao;
import kr.bit.dao.UserDao;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.entity.Message;
import kr.bit.entity.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatService {
    @Autowired
    private ChatDao chatDao;

    @Autowired
    private UserDao userDao;


    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private ChatSocketService chatSocketService;


    public List<ChatRoomDTO> getChatRoomsByUserId(int userId) {
        List<ChatRoom> chatRooms = chatDao.getChatRoomsByUserId(userId);
        if (chatRooms == null) {
            chatRooms = new ArrayList<>();
        }

        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO();
            int roomId = chatRoom.getId();
            dto.setManOut(chatRoom.isManOut());
            dto.setWomanOut(chatRoom.isWomanOut());
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
                profileImageId = 1;
            }

            dto.setNickname(nickname);
            dto.setProfileImageId(profileImageId);
            dto.setUnReadCount(chatDao.getUnReadCountByRoomId(roomId));
            chatRoomDTOs.add(dto);
        }

        return chatRoomDTOs;
    }

    public void chatEnter (int roomId, String gender) {
        if(gender.equals("male")){
            chatDao.updateManEnter(roomId);
        }
        else{
            chatDao.updateWomanEnter(roomId);
        }

        if(chatDao.getBothEnteredStatus(roomId)&&chatDao.getSessionStatus(roomId).getSessionStatus().equals("wait")){
            chatSocketService.notifyRoomUpdate(roomId);
            chatDao.updateChatRoom(roomId,"on",10);
        }
    }

    public void chatStart(ChatRoom chatRoom) {
        chatDao.chatStart(chatRoom);
    }

    public void timeEnd(int roomId, String status) {
        String lockKey = "lock:timeEnd:" + roomId;
        // 고유한 락 식별자 생성 (예: UUID)
        String lockValue = UUID.randomUUID().toString();
        // 락 만료 시간 (초) 설정 - 예: 10초
        int lockExpire = 10;

        try (Jedis jedis = jedisPool.getResource()) {
            // Redis SET 명령어를 사용하여 NX 옵션(키가 없을 때만 설정)과 EX 옵션(만료시간 설정)을 사용
            String lockResult = jedis.set(lockKey, lockValue, SetParams.setParams().nx().ex(lockExpire));
            if (!"OK".equals(lockResult)) {
                // 락을 획득하지 못한 경우 로직을 진행하지 않거나 예외 처리
                System.out.println("다른 프로세스가 처리 중입니다. 잠시 후 다시 시도하세요.");
                return;
            }

            // 락 획득 성공 - 이제 아래의 코드가 동시에 실행되지 않도록 보장됩니다.
            if (status.equals("on")) {
                ChatRoom chatRoom = chatDao.getChatRoom(roomId);
                int manId = chatRoom.getManId();
                int womanId = chatRoom.getWomanId();
                ChatClosure chatClosure1 = new ChatClosure();
                ChatClosure chatClosure2 = new ChatClosure();
                chatClosure1.setRoomId(roomId);
                chatClosure2.setRoomId(roomId);
                chatClosure1.setUserId(manId);
                chatClosure2.setUserId(womanId);
                chatDao.insertChatClosure(chatClosure1);
                chatDao.insertChatClosure(chatClosure2);
                // 채팅방 상태를 "end"로 업데이트 (예: 24*60분 동안 유지)
                chatDao.updateChatRoom(roomId, "end", 24 * 60);

                // 해당 채팅방의 Redis 키 패턴 (예: chat:17:*)
                String redisPattern = "chat:" + roomId + ":*";

                // 패턴에 맞는 모든 Redis 키를 조회
                Set<String> keys = jedis.keys(redisPattern);
                for (String key : keys) {
                    // 각 키에 저장된 해시 데이터를 가져옴
                    Map<String, String> chatData = jedis.hgetAll(key);

                    // Message 객체로 매핑
                    Message message = new Message();
                    message.setRoomId(Integer.parseInt(chatData.get("room_id")));
                    message.setUserId(Integer.parseInt(chatData.get("user_id")));
                    message.setMessageContent(chatData.get("msg"));
                    // created_at은 "yyyy-MM-dd HH:mm:ss" 형식으로 저장되었다고 가정
                    message.setCreatedAt(Timestamp.valueOf(chatData.get("created_at")));
                    // "read" 값이 "true"이면 true, 아니면 false로 처리
                    message.setRead(chatData.get("read").equalsIgnoreCase("true"));

                    // SQL에 메시지 삽입
                    chatDao.insertMessage(message);
                }
            } else {
                if (!chatDao.getSessionStatus(roomId).isReported()) {
                    chatDao.deleteChatRoom(roomId);
                }else {
                    chatDao.manOut(roomId);
                    chatDao.womanOut(roomId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 락을 해제합니다. 단, 해제 전에 현재 락의 value가 본인이 설정한 값인지 확인합니다.
            try (Jedis jedis = jedisPool.getResource()) {
                String luaScript =
                        "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                                "   return redis.call('del', KEYS[1]) " +
                                "else " +
                                "   return 0 " +
                                "end";
                jedis.eval(luaScript, Collections.singletonList(lockKey), Collections.singletonList(lockValue));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public ChatRoom getChatRoom(int roomId){
        return chatDao.getChatRoom(roomId);
    }

    public RoomStatusDTO getSessionStatus(int roomId){return chatDao.getSessionStatus(roomId);}

    public void updateReport(Report report, String gender){
        int roomId = report.getCurrentId();
        chatDao.report(report);
        chatDao.setIsReported(roomId);
        updateOutRoom(roomId,gender);
    }


    public void updateOutRoom(int roomId, String gender) {
        RoomStatusDTO sessionStatus = chatDao.getSessionStatus(roomId);

        if (gender.equals("male")) {
            if (sessionStatus.isWomanOut()) {
                if (!sessionStatus.isReported()) {
                    chatDao.deleteChatRoom(roomId);
                } else {
                    chatDao.manOut(roomId);
                }
            } else {
                chatDao.manOut(roomId);
            }
        } else {
            if (sessionStatus.isManOut()) {
                if (!sessionStatus.isReported()) {
                    chatDao.deleteChatRoom(roomId);
                } else {
                    chatDao.womanOut(roomId);
                }
            } else {
                chatDao.womanOut(roomId);
            }
        }
    }
}
