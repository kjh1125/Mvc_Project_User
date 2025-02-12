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
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private UserService userService;

    @Autowired
    private ChatService chatService;

    @GetMapping("/list")
    public String chatList(Model model, HttpSession session) {
        int userId = (int) session.getAttribute("user");
        System.out.println(userId);
        Point point = userService.getPoint(userId);
        model.addAttribute("firewood", point.getFirewood());
        List<ChatRoomDTO> chatRoomList = chatService.getChatRoomsByUserId(userId);
        System.out.println(chatRoomList);
        model.addAttribute("chatRoomList", chatRoomList);
        return "chat/chatList";
    }

}
