package kr.bit.controller;

import kr.bit.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ChatSocketController {

    @Autowired
    private JedisPool jedisPool;

    // STOMP 메시지를 수신하면 Redis에 저장한 후, 해당 메시지를 /topic/room/{roomId}로 브로드캐스트합니다.
    @MessageMapping("/room/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public String handleChatMessage(@DestinationVariable String roomId, String message,
                                    SimpMessageHeaderAccessor headerAccessor) {        Object userObj = headerAccessor.getSessionAttributes().get("user");
        int userId = userObj != null ? (Integer) userObj : 0;
        System.out.println("메시지:" + message);
        System.out.println("User ID: " + userId);

        // 현재 시간 및 포맷 설정
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createdAt = now.format(formatter);

        // Message 객체를 생성하여 필요한 정보를 설정 (원하는 경우 추가 처리 가능)
        Message msgObj = new Message();
        msgObj.setRoomId(Integer.parseInt(roomId));
        msgObj.setMessageContent(message);
        msgObj.setUserId(userId);
        msgObj.setCreatedAt(Timestamp.valueOf(now));

        Map<String, String> chatData = new HashMap<>();
        chatData.put("room_id", roomId);
        chatData.put("msg", message);
        chatData.put("user_id", String.valueOf(userId));
        chatData.put("created_at", createdAt);
        chatData.put("read", "false");

        String redisKey = "chat:" + roomId + ":" + System.currentTimeMillis();
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(redisKey, chatData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 저장 후 받은 메시지를 그대로 브로드캐스트합니다.
        return message+"userId="+userId;
    }


}
