package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PageController {

    @GetMapping("/tock/{type}")
    public String chatPage(@PathVariable String type) {
        return "redirect:/chat.html?tockId=" + type;
    }
}
