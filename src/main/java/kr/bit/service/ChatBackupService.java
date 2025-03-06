package kr.bit.service;

import kr.bit.dao.ChatDao;
import kr.bit.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ChatBackupService {
    @Autowired
    private ChatDao chatDao;

    @Autowired
    private JedisPool jedisPool;

    @Scheduled(fixedRate = 300000) // ✅ 5분마다 실행
    public void backupChatMessages() {
        System.out.println("✅ 실행됨 (Redis → SQL 백업)");

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> chatRooms = jedis.keys("chat:*"); // ✅ 모든 채팅방 키 가져오기

            if (chatRooms.isEmpty()) return; // ✅ 저장할 메시지가 없으면 종료

            for (String roomKey : chatRooms) {
                Map<String, String> chatMessages = jedis.hgetAll(roomKey); // ✅ Redis에서 채팅방 메시지 가져오기
                if (chatMessages.isEmpty()) continue;
                String[] keyParts = roomKey.split(":");
                int roomId = Integer.parseInt(keyParts[1]);

                for (Map.Entry<String, String> entry : chatMessages.entrySet()) {
                    String messageId = entry.getKey();
                    Map<String, String> chatData = parseMessageData(entry.getValue());
                    // ✅ SQL로 저장된 여부 확인 (is_saved_sql)
                    if ("true".equals(chatData.get("is_saved_sql"))) {
                        continue; // ✅ 이미 저장된 데이터는 건너뛰기
                    }

                    Message message = new Message();
                    message.setRoomId(roomId);
                    message.setUserId(Integer.parseInt(chatData.get("user_id")));
                    message.setMessageContent(chatData.get("msg"));
                    message.setCreatedAt(Timestamp.valueOf(chatData.get("created_at")));
                    message.setRead(Boolean.parseBoolean(chatData.get("read")));

                    chatDao.insertMessage(message); // ✅ SQL 저장
                    chatData.put("is_saved_sql", "true"); // ✅ 저장 여부 업데이트
                    jedis.hset(roomKey, messageId, chatData.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Redis에서 저장된 JSON 메시지를 Map으로 변환하는 메서드
    private Map<String, String> parseMessageData(String messageData) {
        Map<String, String> dataMap = new HashMap<>();
        String[] entries = messageData.replace("{", "").replace("}", "").split(", ");
        for (String entry : entries) {
            String[] keyValue = entry.split("=");
            if (keyValue.length == 2) {
                dataMap.put(keyValue[0], keyValue[1]);
            }
        }
        return dataMap;
    }
}
