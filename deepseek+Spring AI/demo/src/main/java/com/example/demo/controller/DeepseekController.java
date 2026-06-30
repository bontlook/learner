package com.example.demo.controller;

import com.example.demo.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class DeepseekController {

    private final ChatClient chatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    @PostMapping(value = "/deepseek", produces = "text/plain;charset=utf-8")
    public Flux<String> deepseek(@RequestParam String prompt, @RequestParam String tockId) {
        //保存ID
        chatHistoryRepository.save("chat", tockId);

        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, tockId))
                .stream()
                .content();
    }
}
