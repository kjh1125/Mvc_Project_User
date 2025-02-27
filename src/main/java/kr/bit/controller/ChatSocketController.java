package kr.bit.controller;

import kr.bit.dto.MessageDTO;
import kr.bit.entity.Message;
import kr.bit.service.ChatService;
import kr.bit.service.ChatSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
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

    @Autowired
    private ChatSocketService chatSocketService;

    // STOMP 메시지를 수신하면 Redis에 저장한 후, 해당 메시지를 /topic/room/{roomId}로 브로드캐스트합니다.
    @MessageMapping("/room/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public String handleChatMessage(@DestinationVariable String roomId,
                                    @Payload MessageDTO messageDTO,
                                    SimpMessageHeaderAccessor headerAccessor) {
        Object userObj = headerAccessor.getSessionAttributes().get("user");
        int userId = userObj != null ? (Integer) userObj : 0;

        String message = messageDTO.getMsg();
        int receiverId = messageDTO.getReceiverId();

        LocalDateTime now = LocalDateTime.now();
        Timestamp createdAt = Timestamp.valueOf(now);

        chatSocketService.saveMessageToRedis(roomId,userId,message,createdAt);

        chatSocketService.messageUpdate(Integer.parseInt(roomId),receiverId);

        // 저장 후 받은 메시지를 그대로 브로드캐스트합니다.
        return message+"userId="+userId;
    }


}
