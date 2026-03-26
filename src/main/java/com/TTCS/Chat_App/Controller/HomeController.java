package com.TTCS.Chat_App.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String openHomepage() {
        return "homepage";
    }
}
