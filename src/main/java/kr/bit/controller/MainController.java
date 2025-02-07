package kr.bit.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    boolean token = false;
    @GetMapping("/main")
    public String main(){
        if(token){
            return "다른메인";
        }
        return "login";
    }
}
