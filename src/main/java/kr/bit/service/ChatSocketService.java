package kr.bit.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // 특정 방의 모든 사용자에게 WebSocket 메시지를 전송하는 메서드
    public void notifyRoomUpdate(int roomId) {
        String destination = "/topic/room/" + roomId;
        String message = "refresh";  // 클라이언트가 이 메시지를 받으면 새로고침하도록 처리

        messagingTemplate.convertAndSend(destination, message);
        System.out.println("방 " + roomId + "에 새로고침 메시지 전송");
    }
}
