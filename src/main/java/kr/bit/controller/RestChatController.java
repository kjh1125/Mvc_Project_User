package kr.bit.controller;

import kr.bit.service.ChatService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/chat")
public class RestChatController {


    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @PostMapping("/roomEnter")
    public String roomEnter(@RequestParam("room_id") int roomId, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        try {
            // 채팅방 입장 처리
            chatService.updateIsEnter(userId, roomId);
            chatService.isEnter(roomId);
            return "success";
        } catch (Exception e) {
            // 예외 처리
            return "failed " + e.getMessage();
        }
    }

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
                    return "start";
                }
            }
        }
        else{
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.zadd("womanList", currentTime, String.valueOf(userId));
                long manCount = jedis.zcard("manQueue")+jedis.zcard("manList");
                if(manCount > 0) {
                    getMember();
                    return "start";
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
            String manId = jedis.zrange("manQueue", 0, 0).iterator().next();
            String womanId = jedis.zrange("womanQueue", 0, 0).iterator().next();
            chatService.chatStart(manId,womanId);
            // 큐에서 제거
            jedis.zrem("manQueue", manId);
            jedis.zrem("womanQueue", womanId);
        }
    }
}
