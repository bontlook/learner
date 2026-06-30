package com.example.demo.repository;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{

    private final Map<String,List<String>> chatHistory = new HashMap<>();

    @Override
    public void save(String type, String tockId) {
        List<String> chatIds =chatHistory.computeIfAbsent(type, k -> new ArrayList<>());
        if (chatIds.contains(tockId)) {
            return;
        }
        chatIds.add(tockId);
    }

    @Override
    public List<String> getTockIds(String type) {

        return chatHistory.getOrDefault(type, List.of());
    }
}
