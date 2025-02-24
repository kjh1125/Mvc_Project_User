package kr.bit.controller;

import kr.bit.entity.Card;
import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import kr.bit.service.ChatService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    @PostMapping("/end/{roomId}")
    public String roomEnd(@PathVariable("roomId") int roomId, @RequestParam("status") String status) {
        chatService.timeEnd(roomId,status);
        return "success";
    }

    @PostMapping("/flip/{closureId}")
    public ResponseEntity<Map<String, Object>> flip(
            @PathVariable("closureId") int closureId,
            @RequestParam("num") int num,
            @RequestParam("userId") int userId) {
        Card card = chatService.flipCard(userId, closureId, num);
        String cardType = "";
        String cardContent = "";
        List<String> hobbyList = new ArrayList<>();

        if(num == 1){
            cardType = card.getCardType1();
            cardContent = card.getCardContent1();
        } else if(num == 2){
            cardType = card.getCardType2();
            cardContent = card.getCardContent2();
        } else if(num == 3){
            cardType = card.getCardType3();
            cardContent = card.getCardContent3();
        }
        Map<String, Object> response = new HashMap<>();

        response.put("cardType", cardType);
        if(cardType.equals("취미")){
            hobbyList = new ArrayList<>(Arrays.asList(cardContent.split(":")));
            response.put("cardContent", hobbyList);
        }else {
            response.put("cardContent", cardContent);
        }
        return ResponseEntity.ok(response);
    }


    @PostMapping("/markAsRead")
    public ResponseEntity<String> markMessagesAsRead(@RequestParam int roomId, @RequestParam int userId) {
        try {
            // ChatService의 markMessagesAsRead 메서드 호출
            chatService.markMessagesAsRead(roomId, userId);
            return ResponseEntity.ok("Messages marked as read");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking messages as read");
        }
    }


}
