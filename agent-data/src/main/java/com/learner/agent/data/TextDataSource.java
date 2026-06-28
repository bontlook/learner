package com.learner.agent.data;

import com.learner.agent.core.DataSource;
import com.learner.agent.core.ProcessedData;

import java.util.Map;

/**
 * 纯文本数据源 — 直接传入文本或从文件读取
 */
public class TextDataSource implements DataSource<ProcessedData> {

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public boolean supports(String type) {
        return "text".equalsIgnoreCase(type) || "txt".equalsIgnoreCase(type);
    }

    @Override
    public ProcessedData fetch(Map<String, Object> params) {
        String text = (String) params.getOrDefault("text", "");
        String source = (String) params.getOrDefault("source", "direct-input");
        String id = "txt-" + System.currentTimeMillis();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text 参数不能为空");
        }

        return new ProcessedData(id, text, source, "text");
    }
}
