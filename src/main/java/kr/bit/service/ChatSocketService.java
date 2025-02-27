package kr.bit.service;

import com.google.gson.Gson;
import kr.bit.dao.ChatDao;
import kr.bit.dto.ChatRoomDTO;
import kr.bit.entity.ChatRoom;
import kr.bit.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatSocketService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private ChatDao chatDao;
    @Autowired
    private ChatService chatService;

    // 특정 방의 모든 사용자에게 WebSocket 메시지를 전송하는 메서드
    public void notifyRoomUpdate(int roomId) {
        String destination = "/topic/room/" + roomId;
        String message = "refresh";  // 클라이언트가 이 메시지를 받으면 새로고침하도록 처리

        messagingTemplate.convertAndSend(destination, message);
    }

    public void refreshUser(int userId){
        String destination = "/topic/user/" + userId;
        String message= "refresh";

        messagingTemplate.convertAndSend(destination, message);
    }

    public void messageUpdate(int roomId, int userId) {
        String destination = "/topic/user/" + userId;
        chatDao.chatroomToTop(roomId); // ✅ 채팅방 목록에서 해당 채팅방을 상단으로 이동

        try (Jedis jedis = jedisPool.getResource()) {
            String redisKey = "chat:" + roomId; // ✅ 변경된 방식 적용 (단일 키 사용)

            // ✅ 해당 채팅방의 모든 메시지를 가져옴
            Map<String, String> chatMessages = jedis.hgetAll(redisKey);

            int unReadCount = 0;
            String latestMessage = null;
            String latestMessageTime = null;

            // ✅ 메시지 정렬 (타임스탬프 기준)
            List<Map.Entry<String, String>> sortedMessages = new ArrayList<>(chatMessages.entrySet());
            sortedMessages.sort(Comparator.comparingLong(entry -> Long.parseLong(entry.getKey())));

            // ✅ 메시지 순회하며 읽지 않은 메시지 수 & 최신 메시지 찾기
            for (Map.Entry<String, String> entry : sortedMessages) {
                String messageJson = entry.getValue();
                Map<String, String> chatData = parseMessageData(messageJson);

                // ✅ 읽지 않은 메시지 카운트
                if (!chatData.get("user_id").equals(String.valueOf(userId))
                        && chatData.get("read").equals("false")) {
                    unReadCount++;
                }

                // ✅ 최신 메시지 찾기
                String messageTime = chatData.get("created_at");
                if (latestMessageTime == null || messageTime.compareTo(latestMessageTime) > 0) {
                    latestMessageTime = messageTime;
                    latestMessage = chatData.get("msg");
                }
            }

            // ✅ STOMP 메시지를 해당 유저에게 전송
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("roomId", roomId);
            updateData.put("latestMessage", latestMessage);
            updateData.put("unReadCount", unReadCount);

            // ✅ 소켓을 통해 메시지 전송
            messagingTemplate.convertAndSend(destination, updateData);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveMessageToRedis(String roomId, int userId, String message, Timestamp createdAt) {
        String createdAtStr = createdAt.toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String messageId = String.valueOf(System.currentTimeMillis()); // 메시지 ID (타임스탬프)

        Map<String, String> chatData = new HashMap<>();
        chatData.put("user_id", String.valueOf(userId));
        chatData.put("msg", message);
        chatData.put("created_at", createdAtStr);
        chatData.put("read", "false");
        chatData.put("is_saved_redis", "false"); // ✅ Redis 저장 여부 추가

        String redisKey = "chat:" + roomId; // ✅ 방 하나당 하나의 키 사용

        try (Jedis jedis = jedisPool.getResource()) {
            jedis.hset(redisKey, messageId, chatData.toString()); // ✅ 하나의 해시에 메시지를 저장
        }
    }

    public List<Message> getMessagesByRoomId(int roomId) {
        List<Message> messageList = new ArrayList<>();
        String redisKey = "chat:" + roomId; // ✅ 단일 키로 저장된 방의 메시지 가져오기

        try (Jedis jedis = jedisPool.getResource()) {
            // ✅ 해당 채팅방의 모든 메시지를 가져옴 (messageId → messageData)
            Map<String, String> chatMessages = jedis.hgetAll(redisKey);

            // ✅ 메시지를 messageId (타임스탬프) 기준으로 정렬
            List<Map.Entry<String, String>> sortedMessages = new ArrayList<>(chatMessages.entrySet());
            sortedMessages.sort(Comparator.comparingLong(entry -> Long.parseLong(entry.getKey()))); // 오래된 메시지가 먼저

            // ✅ 각 메시지를 Message 객체로 변환
            for (Map.Entry<String, String> entry : sortedMessages) {
                String messageId = entry.getKey();
                String messageJson = entry.getValue(); // JSON 형식으로 저장된 값

                // JSON 파싱 대신, 간단히 ":"로 분리하여 데이터를 가져옴
                Map<String, String> chatData = parseMessageData(messageJson);

                // ✅ Message 객체 생성
                Message message = new Message();
                message.setRoomId(roomId);
                message.setUserId(Integer.parseInt(chatData.get("user_id")));
                message.setMessageContent(chatData.get("msg"));
                message.setCreatedAt(Timestamp.valueOf(chatData.get("created_at")));
                message.setRead(Boolean.parseBoolean(chatData.get("read")));

                // ✅ 리스트에 추가
                messageList.add(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return messageList;
    }

    // ✅ Redis에서 저장한 메시지 데이터를 Map으로 변환하는 메서드
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

    public List<ChatRoomDTO> getChatRoomsByUserId(int userId) {
        List<ChatRoomDTO> chatRoomList = chatService.getChatRoomsByUserId(userId);

        try (Jedis jedis = jedisPool.getResource()) {
            for (ChatRoomDTO chatRoom : chatRoomList) {
                if(chatRoom.getSessionStatus().equals("on")||chatRoom.getSessionStatus().equals("continue")){
                    int roomId = chatRoom.getChatRoomId();
                    String redisKey = "chat:" + roomId; // ✅ 변경된 저장 방식 적용
                    // ✅ 해당 채팅방의 모든 메시지를 가져옴
                    Map<String, String> chatMessages = jedis.hgetAll(redisKey);
                    int unReadCount = 0;
                    String latestMessage = null;
                    String latestMessageTime = null;

                    // ✅ messageId 기준으로 정렬 (최신 메시지를 찾기 위함)
                    List<Map.Entry<String, String>> sortedMessages = new ArrayList<>(chatMessages.entrySet());
                    sortedMessages.sort(Comparator.comparingLong(entry -> Long.parseLong(entry.getKey())));

                    // ✅ 메시지 순회하며 읽지 않은 메시지 수 & 최신 메시지 찾기
                    for (Map.Entry<String, String> entry : sortedMessages) {
                        String messageId = entry.getKey();
                        String messageJson = entry.getValue();
                        Map<String, String> chatData = parseMessageData(messageJson);

                        // ✅ 메시지가 현재 사용자가 보낸 것이 아니고, 읽지 않은 메시지라면 unReadCount 증가
                        if (!chatData.get("user_id").equals(String.valueOf(userId))
                                && chatData.get("read").equals("false")) {
                            unReadCount++;
                        }

                        // ✅ 최신 메시지 업데이트
                        String messageTime = chatData.get("created_at");
                        if (latestMessageTime == null || messageTime.compareTo(latestMessageTime) > 0) {
                            latestMessageTime = messageTime;
                            latestMessage = chatData.get("msg");
                        }
                    }

                    // ✅ 최근 메시지 설정
                    if (latestMessage != null) {
                        chatRoom.setContent(latestMessage);
                    }

                    // ✅ 읽지 않은 메시지 개수 설정
                    if (unReadCount > 0) {
                        chatRoom.setUnReadCount(unReadCount);
                    } else {
                        chatRoom.setUnReadCount(null);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return chatRoomList;
    }

    public void markMessagesAsRead(int roomId, int userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            String redisKey = "chat:" + roomId; // ✅ 단일 키 기반 HSET 사용

            // ✅ 해당 채팅방의 모든 메시지를 가져옴
            Map<String, String> chatMessages = jedis.hgetAll(redisKey);

            if (chatMessages.isEmpty()) return; // ✅ 읽을 메시지가 없으면 종료

            for (Map.Entry<String, String> entry : chatMessages.entrySet()) {
                String messageId = entry.getKey();
                Map<String, String> chatData = parseMessageData(entry.getValue());

                int messageUserId = Integer.parseInt(chatData.get("user_id")); // 보낸 사람 ID
                String messageReadStatus = chatData.get("read"); // 읽음 여부

                // ✅ 받은 사람이 보낸 사람과 다르고, 메시지가 아직 읽히지 않았다면
                if (messageUserId != userId && "false".equals(messageReadStatus)) {
                    chatData.put("read", "true"); // ✅ 읽음 처리
                    jedis.hset(redisKey, messageId, chatData.toString()); // ✅ JSON 변환 후 Redis 업데이트
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error marking messages as read", e); // 예외를 던져서 호출자가 처리할 수 있도록
        }
    }


}