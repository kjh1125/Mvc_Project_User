package kr.bit.entity;

import lombok.Data;

@Data
public class Card {
    int chatEndId;
    String cardType1;
    String cardContent1;
    String cardStatus1= "close";
    String cardType2;
    String cardContent2;
    String cardStatus2= "close";
    String cardType3;
    String cardContent3;
    String cardStatus3= "close";
}
