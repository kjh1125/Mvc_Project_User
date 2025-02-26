package kr.bit.service;

import kr.bit.dao.ChatDao;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.entity.ChatRoom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ChatSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private ChatDao chatDao;

    // 특정 방의 모든 사용자에게 WebSocket 메시지를 전송하는 메서드
    public void notifyRoomUpdate(int roomId) {
        String destination = "/topic/room/" + roomId;
        String message = "refresh";  // 클라이언트가 이 메시지를 받으면 새로고침하도록 처리

        messagingTemplate.convertAndSend(destination, message);
        System.out.println("방 " + roomId + "에 새로고침 메시지 전송");
    }

    public void refreshUser(int userId){
        String destination = "/topic/user/" + userId;
        String message= "refresh";

        messagingTemplate.convertAndSend(destination, message);
        System.out.println("유저"+userId+"에 새로고침 메시지 전송");
    }

    public void messageUpdate(int roomId, int userId) {
        String destination = "/topic/user/" + userId;
        chatDao.chatroomToTop(roomId);
        // Redis에서 roomId에 해당하는 모든 Redis 키 가져오기
        String redisPattern = "chat:" + roomId + ":*";
        try (Jedis jedis = jedisPool.getResource()) { // Redis 리소스를 안전하게 관리
            Set<String> keys = jedis.keys(redisPattern);

            int unReadCount = 0;
            String latestMessage = null; // 최근 메시지
            String latestMessageTime = null; // 최근 메시지의 타임스탬프

            for (String key : keys) {
                Map<String, String> chatData = jedis.hgetAll(key);

                // 메시지가 현재 사용자가 보낸 것이 아니고, 읽지 않은 메시지인 경우
                if (!chatData.get("user_id").equals(String.valueOf(userId))
                        && chatData.get("read").equals("false")) {
                    unReadCount++;
                }

                // 타임스탬프가 최신 메시지인지 확인
                String messageTime = chatData.get("created_at");
                if (latestMessageTime == null || messageTime.compareTo(latestMessageTime) > 0) {
                    latestMessageTime = messageTime;
                    latestMessage = chatData.get("msg"); // 가장 최신 메시지 저장
                }
            }


            // STOMP 메시지를 해당 유저에게 전송
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("roomId", roomId);
            updateData.put("latestMessage", latestMessage);
            updateData.put("unReadCount", unReadCount);

            // 소켓을 통해 메시지 전송
            messagingTemplate.convertAndSend(destination, updateData);
        } catch (Exception e) {
            e.printStackTrace();
            // 예외 처리 추가 (예: 로그 기록, 재시도 등)
        }
    }



}