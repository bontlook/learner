package com.example.demo.repository;


import java.util.List;

public interface ChatHistoryRepository {
    /*
     * 保存会话记录
     * @param type 业务参数
     * @param chat
     */
    void save(String Type,String tockId);

    List<String> getTockIds(String type);
}
