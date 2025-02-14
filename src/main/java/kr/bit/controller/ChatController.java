package kr.bit.controller;

import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import kr.bit.entity.Point;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.service.ChatService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.util.*;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;


    @Autowired
    private JedisPool jedisPool;

    @GetMapping("/list")
    public String chatList(Model model, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        String userIdStr = String.valueOf(userId);
        boolean isWaiting = false;
        try (Jedis jedis = jedisPool.getResource()) {
            isWaiting = jedis.zrank("manList", userIdStr) != null ||
                    jedis.zrank("manQueue", userIdStr) != null   ||
                    jedis.zrank("womanQueue", userIdStr) != null ||
                    jedis.zrank("womanList", userIdStr) != null;
        }

        model.addAttribute("waiting",isWaiting);
        Point point = userService.getPoint(userId);
        model.addAttribute("firewood", point.getFirewood());
        List<ChatRoomDTO> chatRoomList = chatService.getChatRoomsByUserId(userId);
        model.addAttribute("chatRoomList", chatRoomList);
        return "chat/chatList";
    }

    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable("roomId") int roomId, Model model, HttpSession session) {
        // Redis에서 데이터를 가져와 Message 객체로 변환
        List<Message> messageList = getMessagesByRoomId(roomId);
        Collections.reverse(messageList);
        int userId = (int) session.getAttribute("user");
        String gender = (String) session.getAttribute("gender");
        ChatRoom chatRoom = chatService.getChatRoom(roomId);
        int receiverId;
        if(gender.equals("male")){
            receiverId = chatRoom.getWomanId();
        }
        else{
            receiverId = chatRoom.getManId();
        }
        model.addAttribute("roomId", roomId);
        model.addAttribute("messageList", messageList);
        model.addAttribute("userId", userId);
        model.addAttribute("receiverId", receiverId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("endTime", chatRoom.getEndTime());
        System.out.println(roomId);
        System.out.println(messageList);
        System.out.println(userId);
        System.out.println(receiverId);
        System.out.println(chatRoom);
        System.out.println(chatRoom.getEndTime());
        return "chat/chatRoom";  // 뷰 이름
    }


    private List<Message> getMessagesByRoomId(int roomId) {
        List<Message> messageList = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            // Redis에서 해당 roomId에 맞는 채팅 메시지 키 가져오기
            Set<String> chatKeys = jedis.keys("chat:" + roomId + ":*");

            // 키를 타임스탬프 기준으로 내림차순 정렬
            List<String> sortedKeys = new ArrayList<>(chatKeys);
            sortedKeys.sort(Comparator.comparingLong(key -> {
                String[] parts = key.split(":");
                return Long.parseLong(parts[2]); // timestamp 부분을 기준으로 정렬
            })); // 내림차순으로 정렬

            // 각 chat 키에 대해 해시 형태의 데이터를 가져와 Message 객체로 변환
            for (String key : sortedKeys) {
                Map<String, String> chatData = jedis.hgetAll(key);

                // Message 객체 생성 및 값 설정
                Message message = new Message();
                message.setRoomId(Integer.parseInt(chatData.get("room_id"))); // room_id로 변경
                message.setUserId(Integer.parseInt(chatData.get("user_id"))); // user_id로 변경
                message.setMessageContent(chatData.get("msg"));

                // Redis에서 가져온 메시지의 'read' 값을 그대로 사용
                message.setRead(Boolean.parseBoolean(chatData.get("read")));

                // 리스트에 추가
                messageList.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }


    @PostMapping("/roomEnter")
    public String roomEnter(@RequestParam("room_id") int roomId, HttpSession session) {
        String gender = (String) session.getAttribute("gender");
        try {
            chatService.chatEnter(roomId,gender);
            return "redirect:/chat/room/" + roomId;
        } catch (Exception e) {
            // 예외 처리
            return "failed " + e.getMessage();
        }
    }
}
