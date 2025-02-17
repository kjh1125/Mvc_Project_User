package kr.bit.entity;

import lombok.Data;

@Data
public class Report {
    int id;
    int reporterId;
    int reportedId;
    String reportType = "room";
    int currentId;
    String reportContent;
}
