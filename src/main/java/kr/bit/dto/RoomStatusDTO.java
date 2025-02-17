package kr.bit.dto;

import lombok.Data;

@Data
public class RoomStatusDTO {
    private String sessionStatus;
    private boolean manOut;
    private boolean womanOut;
    private int roomId;
    private boolean reported;
}
