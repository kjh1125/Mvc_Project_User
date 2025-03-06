package kr.bit.service;

import kr.bit.dao.ChatDao;
import kr.bit.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatRestoreService {
    @Autowired
    private ChatDao chatDao;

    @Autowired
    private JedisPool jedisPool;

    public void restoreChatMessages(int userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // ✅ SQL에서 해당 userId의 메시지 가져오기
            List<Integer> chatRoomList = chatDao.getChatroomListByUserId(userId);
            List<Message> messages = new ArrayList<>();
            for(Integer chatRoomId : chatRoomList) {
                messages.addAll(chatDao.getMessagesByRoomId(chatRoomId));
            }
            System.out.println(messages);
            for (Message message : messages) {
                String redisKey = "chat:" + message.getRoomId(); // ✅ 하나의 키에 저장 (기존 방식 제거)
                String messageId = String.valueOf(message.getCreatedAt().getTime()); // ✅ 메시지 ID (timestamp)

                // ✅ 이미 Redis에 저장된 메시지인지 확인
                if (jedis.hexists(redisKey, messageId)) {
                    continue; // 이미 존재하면 건너뛰기
                }

                // ✅ Redis에 저장할 메시지 데이터 구성
                Map<String, String> chatData = new HashMap<>();
                chatData.put("user_id", String.valueOf(message.getUserId()));
                chatData.put("msg", message.getMessageContent());
                chatData.put("created_at", message.getCreatedAt().toString());
                chatData.put("read", String.valueOf(message.isRead()));
                chatData.put("is_saved_redis", "true"); // ✅ Redis 저장 여부 추가

                // ✅ Redis에 저장
                jedis.hset(redisKey, messageId, chatData.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
