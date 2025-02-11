package kr.bit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BoardController {

    @GetMapping(value = "/boardList")
    public String boardList(){

        return "board/boardList";
    }

}
