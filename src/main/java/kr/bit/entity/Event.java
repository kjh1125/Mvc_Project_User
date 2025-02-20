package kr.bit.entity;

import lombok.Data;

@Data
public class Event {
    private int id;
    private String name;
    private String imageUrl;
    private String startDate;
    private String endDate;
}
