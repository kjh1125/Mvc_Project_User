package kr.bit.controller;

import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import kr.bit.service.ChatService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class RestChatController {


    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;



    @PostMapping("/ping")
    public String ping(HttpSession session) {
        try (Jedis jedis = new Jedis("223.130.151.175", 6379)) {  // try-with-resources로 Jedis 자동 종료
            List<String> ping = jedis.zrandmember("manList",10);  // PING 명령
            System.out.println(ping);
            return "hello";  // "PONG" 반환
        } catch (Exception e) {
            System.out.println("Redis connection error: " + e.getMessage());
            return "Error: Unable to connect to Redis server";
        }
    }

    @PostMapping("/matchStart")
    public String matchStart(HttpSession session) {
        int userId = (int) session.getAttribute("user");
        userService.updateFirewood(userId);
        String gender = userService.getGender(userId);
        long currentTime = System.currentTimeMillis();
        if(gender.equals("male")) {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zadd("manList", currentTime, String.valueOf(userId));
                long womanCount = jedis.zcard("womanQueue")+jedis.zcard("womanList");
                if(womanCount > 0) {
                    getMember();
                    return "success";
                }
            }
        }
        else{
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zadd("womanList", currentTime, String.valueOf(userId));
                long manCount = jedis.zcard("manQueue")+jedis.zcard("manList");
                if(manCount > 0) {
                    getMember();
                    return "success";
                }
            }
        }
        return "wait";
    }

    public void getMember() {
        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.zcard("manQueue") == 0) {
                List<String> manMembers = jedis.zrandmember("manList", 10);
                if (manMembers != null && !manMembers.isEmpty()) {
                    for (String userId : manMembers) {
                        jedis.zadd("manQueue", System.currentTimeMillis(), userId);
                        jedis.zrem("manList", userId); // manList에서 제거
                    }
                }
            }

            if (jedis.zcard("womanQueue") == 0) {
                // womanList에서 10명 뽑아서 womanQueue에 추가
                List<String> womanMembers = jedis.zrandmember("womanList", 10);
                if (womanMembers != null && !womanMembers.isEmpty()) {
                    for (String userId : womanMembers) {
                        jedis.zadd("womanQueue", System.currentTimeMillis(), userId);
                        jedis.zrem("womanList", userId); // womanList에서 제거
                    }
                }
            }

            // 큐에서 제일 앞에 있는 man과 woman의 userId 반환
            String manIdStr = jedis.zrange("manQueue", 0, 0).iterator().next();
            String womanIdStr = jedis.zrange("womanQueue", 0, 0).iterator().next();
            int manId = Integer.parseInt(manIdStr);
            int womanId = Integer.parseInt(womanIdStr);
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setManId(manId);
            chatRoom.setWomanId(womanId);
            chatService.chatStart(chatRoom);
            jedis.zrem("manQueue", manIdStr);
            jedis.zrem("womanQueue", womanIdStr);
        }
    }

    @PostMapping("/send/{roomId}")
    public String send(@PathVariable("roomId") int roomId,
                       @RequestParam("msg") String msg,
                       @RequestParam("receiverId")int receiverId,
                       HttpSession session) {
        int userId = (int) session.getAttribute("user"); // 세션에서 userId 가져오기
        System.out.println(userId);

        try (Jedis jedis = jedisPool.getResource()) {
            // 현재 시간을 포맷팅하여 생성 시간 설정
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String createdAt = now.format(formatter);

            // Message 객체 생성
            Message message = new Message();
            message.setRoomId(roomId);
            message.setMessageContent(msg);
            message.setUserId(userId);
            message.setCreatedAt(Timestamp.valueOf(now)); // Timestamp로 변환

            // Redis에 저장할 데이터 준비
            Map<String, String> chatData = new HashMap<>();
            chatData.put("room_id", String.valueOf(message.getRoomId()));
            chatData.put("msg", message.getMessageContent());
            chatData.put("user_id", String.valueOf(message.getUserId()));
            chatData.put("created_at", createdAt);
            chatData.put("read", String.valueOf(message.isRead()));
            chatData.put("receiver_id", String.valueOf(receiverId));

            String redisKey = "chat:" + roomId + ":" + System.currentTimeMillis();
            jedis.hset(redisKey, chatData);  // hmset -> hset으로 변경

            return "Message sent successfully!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error sending message.";
        }

    }

    @PostMapping("/end/{roomId}")
    public String roomEnd(@PathVariable("roomId") int roomId, @RequestParam("status") String status) {
        chatService.timeEnd(roomId,status);
        return "success";
    }

}
