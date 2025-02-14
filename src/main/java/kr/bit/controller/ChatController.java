package kr.bit.controller;

import kr.bit.beans.ChatRoom;
import kr.bit.beans.Point;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.service.ChatService;
import kr.bit.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpSession;
import java.util.List;

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

    @GetMapping("/room")
    public String chatRoom(Model model, HttpSession session) {
        return "chat/chatRoom";
    }

}
