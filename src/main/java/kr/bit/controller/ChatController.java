package kr.bit.controller;

import kr.bit.dto.RoomStatusDTO;
import kr.bit.entity.*;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.service.ChatService;
import kr.bit.service.ChatSocketService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.sql.SQLOutput;
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

    @Autowired
    private ChatSocketService chatSocketService;
    @Autowired
    private UserController userController;

    @GetMapping("/list")
    public String chatList(Model model, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        String userIdStr = String.valueOf(userId);
        boolean isWaiting = false;

        // Redis 리소스 요청
        try (Jedis jedis = jedisPool.getResource()) {
            // 대기 상태 확인
            isWaiting = jedis.zrank("manList", userIdStr) != null ||
                    jedis.zrank("manQueue", userIdStr) != null   ||
                    jedis.zrank("womanQueue", userIdStr) != null ||
                    jedis.zrank("womanList", userIdStr) != null;

            // 포인트 정보 가져오기
            Point point = userService.getPoint(userId);
            model.addAttribute("firewood", point.getFirewood());

            // 채팅방 목록 및 읽지 않은 메시지 수 가져오기
            List<ChatRoomDTO> chatRoomList = getChatRoomsByUserId(userId, jedis);
            model.addAttribute("chatRoomList", chatRoomList);
        }

        model.addAttribute("waiting", isWaiting);
        model.addAttribute("userId", userId);
        return "chat/chatList";
    }

    // Redis에서 unread count를 포함한 채팅방 목록 가져오기
    private List<ChatRoomDTO> getChatRoomsByUserId(int userId, Jedis jedis) {
        List<ChatRoomDTO> chatRoomList = chatService.getChatRoomsByUserId(userId);

        for (ChatRoomDTO chatRoom : chatRoomList) {
            int roomId = chatRoom.getChatRoomId();

            // Redis에서 roomId에 해당하는 모든 Redis 키 가져오기
            String redisPattern = "chat:" + roomId + ":*";
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

            // 최근 메시지 설정
            if (latestMessage != null) {
                chatRoom.setContent(latestMessage); // content에 최근 메시지 설정
            }

            // 채팅방에 읽지 않은 메시지 개수 설정
            if(unReadCount>0) {
                chatRoom.setUnReadCount(unReadCount);
            }else{
                chatRoom.setUnReadCount(null);
            }
        }

        return chatRoomList;
    }


    @GetMapping("/room/{roomId}")
    public String chatRoom(@PathVariable("roomId") int roomId, Model model, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        ChatRoom chatRoom = chatService.getChatRoom(roomId);
        if(chatRoom.getWomanId()!=userId&&chatRoom.getManId()!=userId){
            return "redirect:/chat/list";
        }
        String gender = (String) session.getAttribute("gender");
        RoomStatusDTO roomStatus = chatService.getSessionStatus(roomId);
        if(gender.equals("male")&&roomStatus.isManOut()){
            return "redirect:/chat/list";
        }
        if(gender.equals("female")&&roomStatus.isWomanOut()){
            return "redirect:/chat/list";
        }
        String sessionStatus = roomStatus.getSessionStatus();
        if(sessionStatus.equals("end")){
            return "redirect:/chat/end/"+roomId;
        }

        List<Message> messageList = getMessagesByRoomId(roomId);
        Collections.reverse(messageList);
        int receiverId;
        if(gender.equals("male")){
            receiverId = chatRoom.getWomanId();
        }
        else{
            receiverId = chatRoom.getManId();
        }
        String nickname = userService.getNickname(receiverId);
        int profileImageId = userService.getProfileImage(receiverId);
        if(roomStatus.isManOut()|| roomStatus.isWomanOut()){
            model.addAttribute("out", true);
        }
        else{
            model.addAttribute("out", false);
        }
        chatService.markMessagesAsRead(roomId,userId);

        model.addAttribute("roomId", roomId);
        model.addAttribute("messageList", messageList);
        model.addAttribute("userId", userId);
        model.addAttribute("profileImageId", profileImageId);
        model.addAttribute("chatRoom", chatRoom);
        model.addAttribute("endTime", chatRoom.getEndTime());
        model.addAttribute("status", sessionStatus);
        model.addAttribute("receiverId", receiverId);
        model.addAttribute("nickname", nickname);
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

    @PostMapping("/out/{roomId}")
    public String outRoom(@PathVariable("roomId") int roomId, Model model, HttpSession session) {
        String gender = (String) session.getAttribute("gender");
        chatService.updateOutRoom(roomId,gender);
        chatSocketService.notifyRoomUpdate(roomId);
        return "redirect:/chat/list";  // 뷰 이름
    }

    @PostMapping("/report/{roomId}")
    public String reportChat(@PathVariable int roomId, @RequestParam String reason, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        String gender = (String) session.getAttribute("gender");
        ChatRoom chatRoom = chatService.getChatRoom(roomId);
        if(userId==chatRoom.getWomanId()||userId==chatRoom.getManId()) {
            int receiverId;
            if (gender.equals("male")) {
                receiverId = chatRoom.getWomanId();
            } else {
                receiverId = chatRoom.getManId();
            }
            Report report = new Report();
            report.setReporterId(userId);
            report.setReportContent(reason);
            report.setCurrentId(roomId);
            report.setReportedId(receiverId);
            chatService.updateReport(report, gender);
        }
        return "redirect:/chat/list";
    }

    @GetMapping("/end/{roomId}")
    public String endRoom(@PathVariable("roomId") int roomId, Model model, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        String gender = (String) session.getAttribute("gender");
        RoomStatusDTO roomStatus = chatService.getSessionStatus(roomId);
        if(!roomStatus.getSessionStatus().equals("end")){
            return "redirect:/chat/room/" + roomId;
        }
        boolean isContinue;
        if(gender.equals("male")) {
            isContinue = roomStatus.isManContinue();
        }
        else{
            isContinue = roomStatus.isWomanContinue();
        }
        ChatClosure chatClosure = new ChatClosure();
        chatClosure.setUserId(userId);
        chatClosure.setRoomId(roomId);
        int closureId = chatService.getClosureId(chatClosure);
        String otherNickname = chatService.getOppositeNickname(chatClosure);
        Card card = chatService.getCardByChatEndId(closureId);
        int readingGlass = userService.getPoint(userId).getReadingGlass();
        List<String> hobbyList;
        if(card.getCardStatus1().equals("open")){
            model.addAttribute("cardType1", card.getCardType1());
            if(card.getCardType1().equals("취미")){
                hobbyList = new ArrayList<>(Arrays.asList(card.getCardContent1().split(":")));
                if(hobbyList.isEmpty()){
                    hobbyList.add("없음");
                }
                model.addAttribute("cardContent1", hobbyList);
            }else{
                model.addAttribute("cardContent1", card.getCardContent1());
            }
        }
        if(card.getCardStatus2().equals("open")){
            model.addAttribute("cardType2", card.getCardType2());
            if(card.getCardType2().equals("취미")){
                hobbyList = new ArrayList<>(Arrays.asList(card.getCardContent2().split(":")));
                if(hobbyList.isEmpty()){
                    hobbyList.add("없음");
                }
                model.addAttribute("cardContent2", hobbyList);
            }else{
                model.addAttribute("cardContent2", card.getCardContent2());
            }
        }
        if(card.getCardStatus3().equals("open")){
            model.addAttribute("cardType3", card.getCardType3());
            if(card.getCardType3().equals("취미")){
                hobbyList = new ArrayList<>(Arrays.asList(card.getCardContent3().split(":")));
                if(hobbyList.isEmpty()){
                    hobbyList.add("없음");
                }
                model.addAttribute("cardContent3", hobbyList);
            }else{
                model.addAttribute("cardContent3", card.getCardContent3());
            }
        }
        if(card.getCardStatus1().equals("close")&&card.getCardStatus2().equals("close")&&card.getCardStatus3().equals("close")){
            model.addAttribute("freeChance", true);
        }
        else{
            model.addAttribute("freeChance", false);
        }
        model.addAttribute("isContinue",isContinue);
        model.addAttribute("readingGlass", readingGlass);
        model.addAttribute("userId",userId);
        model.addAttribute("endTime", chatService.getChatRoom(roomId).getEndTime());
        model.addAttribute("otherNickname", otherNickname);
        model.addAttribute("closureId", closureId);
        return "chat/endRoom";  // 뷰 이름
    }

    @PostMapping("/continue/{roomId}")
    public String chatContinue(@PathVariable int roomId, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        String gender = (String) session.getAttribute("gender");
        chatService.userContinue(gender, roomId);
        return "redirect:/chat/end/" + roomId;
    }

}
