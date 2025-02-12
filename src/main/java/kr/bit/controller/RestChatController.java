package kr.bit.controller;

import kr.bit.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/chat")
public class RestChatController {

    @Autowired
    private ChatService chatService;

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
}
