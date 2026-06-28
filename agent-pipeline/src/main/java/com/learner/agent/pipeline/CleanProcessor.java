package com.learner.agent.pipeline;

import com.learner.agent.core.ProcessedData;
import com.learner.agent.core.Processor;

import java.util.Map;

/**
 * 文本清理处理器 — 去除噪声、标准化格式
 */
public class CleanProcessor implements Processor {

    @Override
    public String getName() {
        return "clean";
    }

    @Override
    public ProcessedData process(ProcessedData input, Map<String, Object> config) {
        String text = input.getText();
        if (text == null || text.isEmpty()) {
            return input;
        }

        // 1. 移除 HTML 标签（如果有残留）
        text = text.replaceAll("<[^>]+>", " ");

        // 2. 统一换行为 \n
        text = text.replace("\r\n", "\n").replace("\r", "\n");

        // 3. 压缩连续空白（保留换行）
        text = text.replaceAll("[ \t ]+", " ");

        // 4. 压缩连续空行
        text = text.replaceAll("\n{3,}", "\n\n");

        // 5. 去除首尾空白
        text = text.trim();

        // 6. 统一标点（中文逗号、引号等标准化）
        text = text.replace("“", "\"")  // 左双引号
                   .replace("”", "\"")   // 右双引号
                   .replace("‘", "'")    // 左单引号
                   .replace("’", "'");   // 右单引号

        input.setText(text);
        input.addMeta("cleaned", true);
        input.addMeta("textLength", text.length());
        return input;
    }
}
