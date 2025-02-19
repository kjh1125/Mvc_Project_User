package kr.bit.service;

import kr.bit.dto.RoomStatusDTO;
import kr.bit.entity.*;
import kr.bit.dao.ChatDao;
import kr.bit.dao.UserDao;
import kr.bit.dto.ChatRoomDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ChatService {
    @Autowired
    private ChatDao chatDao;

    @Autowired
    private UserDao userDao;


    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private ChatSocketService chatSocketService;


    public List<ChatRoomDTO> getChatRoomsByUserId(int userId) {
        List<ChatRoom> chatRooms = chatDao.getChatRoomsByUserId(userId);
        if (chatRooms == null) {
            chatRooms = new ArrayList<>();
        }

        List<ChatRoomDTO> chatRoomDTOs = new ArrayList<>();

        for (ChatRoom chatRoom : chatRooms) {
            ChatRoomDTO dto = new ChatRoomDTO();
            int roomId = chatRoom.getId();
            dto.setManOut(chatRoom.isManOut());
            dto.setWomanOut(chatRoom.isWomanOut());
            dto.setChatRoomId(roomId);
            dto.setSessionStatus(chatRoom.getSessionStatus());
            Timestamp endTime = chatRoom.getEndTime();
            if (endTime != null) {
                ZonedDateTime zonedDateTime = endTime.toInstant()
                        .atZone(ZoneId.of("Asia/Seoul"));  // 한국 시간대로 변환

                String endTimeFormatted = zonedDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME); // ISO 8601 형식으로 포맷
                dto.setEndTime(endTimeFormatted);
            }

            String defaultMessage = "매칭되었습니다! 방에 참가하세요";
            switch (chatRoom.getSessionStatus()) {
                case "wait":
                    dto.setContent(defaultMessage);
                    break;
                case "on":
                case "continue":
                    String lastChat = null;
                    try {
                        lastChat = chatDao.getLastMessageContentByRoomId(roomId);
                    } catch (Exception e) {
                        lastChat = null;
                    }

                    if (lastChat != null) {
                        dto.setContent(lastChat);
                    } else {
                        dto.setContent(defaultMessage);
                    }
                    break;
                case "end":
                    dto.setContent("대화가 종료되었습니다.");
                    break;
                default:
                    break;
            }

            int otherUserId = (chatRoom.getManId() == userId) ? chatRoom.getWomanId() : chatRoom.getManId();

            // 상대방의 nickname과 profileImageId 가져오기
            String nickname = "알 수 없는 사용자";
            int profileImageId = 0;
            try {
                nickname = userDao.getNickname(otherUserId);
                profileImageId = userDao.getProfileImage(otherUserId);
            } catch (Exception e) {
                // 예외 처리: 기본값 설정
                nickname = "알 수 없는 사용자";
                profileImageId = 1;
            }

            dto.setNickname(nickname);
            dto.setProfileImageId(profileImageId);
            dto.setUnReadCount(chatDao.getUnReadCountByRoomId(roomId));
            chatRoomDTOs.add(dto);
        }

        return chatRoomDTOs;
    }

    public void chatEnter (int roomId, String gender) {
        if(gender.equals("male")){
            chatDao.updateManEnter(roomId);
        }
        else{
            chatDao.updateWomanEnter(roomId);
        }

        if(chatDao.getBothEnteredStatus(roomId)&&chatDao.getSessionStatus(roomId).getSessionStatus().equals("wait")){
            chatSocketService.notifyRoomUpdate(roomId);
            chatDao.updateChatRoom(roomId,"on",10);
        }
    }

    public void chatStart(ChatRoom chatRoom) {
        chatDao.chatStart(chatRoom);
        int user1Id = chatRoom.getManId();
        int user2Id = chatRoom.getWomanId();
        chatSocketService.refreshUser(user1Id);
        chatSocketService.refreshUser(user2Id);
    }

    @Transactional
    public void timeEnd(int roomId, String status) {

            RoomStatusDTO roomStatusDTO = chatDao.getSessionStatus(roomId);

            if (status.equals("on")&&roomStatusDTO.getSessionStatus().equals("on")) {
                chatDao.updateChatRoom(roomId, "end", 24 * 60);
                ChatRoom chatRoom = chatDao.getChatRoom(roomId);
                int manId = chatRoom.getManId();
                int womanId = chatRoom.getWomanId();
                ChatClosure chatClosure1 = new ChatClosure();
                ChatClosure chatClosure2 = new ChatClosure();
                chatClosure1.setRoomId(roomId);
                chatClosure2.setRoomId(roomId);
                chatClosure1.setUserId(manId);
                chatClosure2.setUserId(womanId);
                int result1 = chatDao.insertChatClosure(chatClosure1);
                if (result1 == 0) {
                    return; // 첫번째 삽입이 거부되었을 경우 바로 종료
                }
                int result2 = chatDao.insertChatClosure(chatClosure2);
                if (result2 == 0) {
                    return; // 두번째 삽입이 거부되었을 경우 바로 종료
                }
                if(createCard(chatClosure1.getId(),womanId)!=0){
                    return;
                };
                if(createCard(chatClosure2.getId(),manId)!=0){
                    return;
                };

                // 해당 채팅방의 Redis 키 패턴 (예: chat:17:*)
                String redisPattern = "chat:" + roomId + ":*";
                try {
                    Jedis jedis = jedisPool.getResource();
                    // 패턴에 맞는 모든 Redis 키를 조회
                    Set<String> keys = jedis.keys(redisPattern);

                    if (keys.isEmpty()) {
                        return; // 채팅 기록이 없다면 그냥 종료
                    }

                    for (String key : keys) {
                        // 각 키에 저장된 해시 데이터를 가져옴
                        Map<String, String> chatData = jedis.hgetAll(key);

                        // Message 객체로 매핑
                        Message message = new Message();
                        message.setRoomId(Integer.parseInt(chatData.get("room_id")));
                        message.setUserId(Integer.parseInt(chatData.get("user_id")));
                        message.setMessageContent(chatData.get("msg"));
                        // created_at은 "yyyy-MM-dd HH:mm:ss" 형식으로 저장되었다고 가정
                        message.setCreatedAt(Timestamp.valueOf(chatData.get("created_at")));
                        // "read" 값이 "true"이면 true, 아니면 false로 처리
                        message.setRead(chatData.get("read").equalsIgnoreCase("true"));

                        // SQL에 메시지 삽입
                        chatDao.insertMessage(message);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if(status.equals("end")&&roomStatusDTO.getSessionStatus().equals("end")) {
                if (!chatDao.getSessionStatus(roomId).isReported()) {
                    chatDao.deleteChatRoom(roomId);
                }else {
                    chatDao.manOut(roomId);
                    chatDao.womanOut(roomId);
                }
            }
    }

    private int createCard(int chatEndId, int otherId) {
        Random rand = new Random();
        Set<Integer> uniqueNumbers = new HashSet<>();

        // 9개 중에서 겹치지 않는 3개 숫자 뽑기
        while (uniqueNumbers.size() < 3) {
            int number = rand.nextInt(9) + 1;
            uniqueNumbers.add(number);
        }
        UserProfile userProfile = userDao.getProfile(otherId);

        List<String> userHobbyList = userDao.getUserHobbies(otherId);
        Card card = new Card();
        card.setChatEndId(chatEndId);
        int index = 0;
        for (int number : uniqueNumbers) {
            index++;
            if (number == 1) {
                String birthDateString = userProfile.getBirthDate();
                LocalDate birthDate = LocalDate.parse(birthDateString);
                LocalDate currentDate = LocalDate.now();
                Period period = Period.between(birthDate, currentDate);
                String age = String.valueOf(period.getYears());
                if (index == 1) {
                    card.setCardType1("나이");
                    card.setCardContent1(age);
                } else if (index == 2) {
                    card.setCardType2("나이");
                    card.setCardContent2(age);
                } else if (index == 3) {
                    card.setCardType3("나이");
                    card.setCardContent3(age);
                }
            } else if (number == 2) {
                String height = String.valueOf(userProfile.getHeight());
                if (index == 1) {
                    card.setCardType1("키");
                    card.setCardContent1(height);
                } else if (index == 2) {
                    card.setCardType2("키");
                    card.setCardContent2(height);
                } else if (index == 3) {
                    card.setCardType3("키");
                    card.setCardContent3(height);
                }
            } else if (number == 3) {
                String weight = String.valueOf(userProfile.getWeight());
                if (index == 1) {
                    card.setCardType1("몸무게");
                    card.setCardContent1(weight);
                } else if (index == 2) {
                    card.setCardType2("몸무게");
                    card.setCardContent2(weight);
                } else if (index == 3) {
                    card.setCardType3("몸무게");
                    card.setCardContent3(weight);
                }
            } else if (number == 4) {
                String photoUrl = userProfile.getPhotoImageUrl();
                if (index == 1) {
                    card.setCardType1("사진");
                    card.setCardContent1(photoUrl);
                } else if (index == 2) {
                    card.setCardType2("사진");
                    card.setCardContent2(photoUrl);
                } else if (index == 3) {
                    card.setCardType3("사진");
                    card.setCardContent3(photoUrl);
                }
            } else if (number == 5) {
                StringBuilder hobby = new StringBuilder();
                if (userHobbyList.isEmpty()) {
                    hobby.append("없음");
                } else {
                    for (int i = 0; i < userHobbyList.size(); i++) {
                        hobby.append(userHobbyList.get(i));
                        if (i < userHobbyList.size() - 1) {
                            hobby.append(":");
                        }
                    }
                }
                if (index == 1) {
                    card.setCardType1("취미");
                    card.setCardContent1(hobby.toString());
                } else if (index == 2) {
                    card.setCardType2("취미");
                    card.setCardContent2(hobby.toString());
                } else if (index == 3) {
                    card.setCardType3("취미");
                    card.setCardContent3(hobby.toString());
                }
            } else if (number == 6) {
                String religion = userProfile.getReligion();
                if (index == 1) {
                    card.setCardType1("종교");
                    card.setCardContent1(religion);
                } else if (index == 2) {
                    card.setCardType2("종교");
                    card.setCardContent2(religion);
                } else if (index == 3) {
                    card.setCardType3("종교");
                    card.setCardContent3(religion);
                }
            } else if (number == 7) {
                String smokingStatus = userProfile.getSmokingStatus();
                if (index == 1) {
                    card.setCardType1("흡연");
                    card.setCardContent1(smokingStatus);
                } else if (index == 2) {
                    card.setCardType2("흡연");
                    card.setCardContent2(smokingStatus);
                } else if (index == 3) {
                    card.setCardType3("흡연");
                    card.setCardContent3(smokingStatus);
                }
            } else if (number == 8) {
                String drinkingLevel = userProfile.getDrinkingLevel();
                if (index == 1) {
                    card.setCardType1("음주");
                    card.setCardContent1(drinkingLevel);
                } else if (index == 2) {
                    card.setCardType2("음주");
                    card.setCardContent2(drinkingLevel);
                } else if (index == 3) {
                    card.setCardType3("음주");
                    card.setCardContent3(drinkingLevel);
                }
            } else if (number == 9) {
                String mbti = userProfile.getMbti();
                if (index == 1) {
                    card.setCardType1("MBTI");
                    card.setCardContent1(mbti);
                } else if (index == 2) {
                    card.setCardType2("MBTI");
                    card.setCardContent2(mbti);
                } else if (index == 3) {
                    card.setCardType3("MBTI");
                    card.setCardContent3(mbti);
                }
            }
        }
        chatDao.insertCard(card);
        return 0;
    }


    public ChatRoom getChatRoom(int roomId){
        return chatDao.getChatRoom(roomId);
    }

    public RoomStatusDTO getSessionStatus(int roomId){return chatDao.getSessionStatus(roomId);}

    public void updateReport(Report report, String gender){
        int roomId = report.getCurrentId();
        chatDao.report(report);
        chatDao.setIsReported(roomId);
        updateOutRoom(roomId,gender);
    }


    public void updateOutRoom(int roomId, String gender) {
        RoomStatusDTO sessionStatus = chatDao.getSessionStatus(roomId);

        if (gender.equals("male")) {
            if (sessionStatus.isWomanOut()) {
                if (!sessionStatus.isReported()) {
                    chatDao.deleteChatRoom(roomId);
                } else {
                    chatDao.manOut(roomId);
                }
            } else {
                chatDao.manOut(roomId);
            }
        } else {
            if (sessionStatus.isManOut()) {
                if (!sessionStatus.isReported()) {
                    chatDao.deleteChatRoom(roomId);
                } else {
                    chatDao.womanOut(roomId);
                }
            } else {
                chatDao.womanOut(roomId);
            }
        }
    }

    public int getClosureId(ChatClosure chatClosure){
        return chatDao.getClosureId(chatClosure);
    }

    public String getOppositeNickname(ChatClosure chatClosure){return chatDao.getOppositeNickname(chatClosure);}

    public Card getCardByChatEndId(int chatEndId){return chatDao.getCardByChatEndId(chatEndId);}

    public Card flipCard(int userId, int closureId,int cardNumber){
        Card cardCheck = chatDao.getCardByChatEndId(closureId);
        Card returnCard = new Card();
        if(cardCheck.getCardStatus1().equals("open")||cardCheck.getCardStatus2().equals("open")||cardCheck.getCardStatus3().equals("open")){
            if(userDao.getPoint(userId).getReadingGlass()>0){
                userDao.updateReadingGlass(userId);
                chatDao.updateCardStatus(closureId,cardNumber);
                if(cardNumber==1){
                    returnCard.setCardType1(cardCheck.getCardType1());
                    returnCard.setCardContent1(cardCheck.getCardContent1());
                }
                if(cardNumber==2){
                    returnCard.setCardType2(cardCheck.getCardType2());
                    returnCard.setCardContent2(cardCheck.getCardContent2());
                }
                if(cardNumber==3){
                    returnCard.setCardType3(cardCheck.getCardType3());
                    returnCard.setCardContent3(cardCheck.getCardContent3());
                }
            }
        }
        else{
            chatDao.updateCardStatus(closureId,cardNumber);
            return cardCheck;
        }
        return returnCard;
    }

    public void userContinue(String gender, int roomId){
        if(gender.equals("male")){
            chatDao.manContinue(roomId);
        }
        else{
            chatDao.womanContinue(roomId);
        }
        RoomStatusDTO sessionStatus = chatDao.getSessionStatus(roomId);
        if(sessionStatus.isWomanContinue()&&sessionStatus.isManContinue()){
            chatDao.updateChatRoom(roomId, "continue", 9999);
            int user1 = chatDao.getChatRoom(roomId).getManId();
            int user2 = chatDao.getChatRoom(roomId).getWomanId();
            chatSocketService.refreshUser(user1);
            chatSocketService.refreshUser(user2);
        }
    }

    public void markMessagesAsRead(int roomId, int userId) {
        try (Jedis jedis = jedisPool.getResource()) {
            // Redis에 저장된 모든 메시지를 조회하여 `roomId`와 `userId`가 다르고 `read` 값이 false인 경우에만 처리
            Set<String> keys = jedis.keys("chat:" + roomId + ":*"); // roomId에 해당하는 모든 메시지 키 가져오기
            for (String key : keys) {
                Map<String, String> chatData = jedis.hgetAll(key);
                int messageUserId = Integer.parseInt(chatData.get("user_id")); // 보낸 사람의 ID
                String messageReadStatus = chatData.get("read"); // 읽음 여부

                // 받은 사람이 보낸 사람과 다르고, 메시지가 아직 읽히지 않았다면
                if (messageUserId!=(userId) && messageReadStatus.equals("false")) {
                    chatData.put("read", "true"); // 읽음 처리
                    jedis.hset(key, chatData); // Redis에서 업데이트
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error marking messages as read", e); // 예외를 던져서 호출자가 처리할 수 있도록
        }
    }
}
