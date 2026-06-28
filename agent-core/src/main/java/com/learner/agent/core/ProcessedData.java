package com.learner.agent.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流水线中流转的数据对象
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessedData {

    /** 数据 ID */
    private String id;
    /** 原始文本 */
    private String rawText;
    /** 当前文本（经过处理后） */
    private String text;
    /** 数据来源描述 */
    private String source;
    /** 数据类型：text/json/csv/web */
    private String type;
    /** 分段后的文本块 */
    private List<String> chunks = new ArrayList<>();
    /** 中间处理结果 */
    private Map<String, Object> metadata = new HashMap<>();
    /** 创建时间 */
    private LocalDateTime createdAt = LocalDateTime.now();

    public ProcessedData() {}

    public ProcessedData(String rawText) {
        this.rawText = rawText;
        this.text = rawText;
    }

    public ProcessedData(String id, String rawText, String source, String type) {
        this.id = id;
        this.rawText = rawText;
        this.text = rawText;
        this.source = source;
        this.type = type;
    }

    // ===== Getters & Setters =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public List<String> getChunks() { return chunks; }
    public void setChunks(List<String> chunks) { this.chunks = chunks; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** 添加元数据 */
    public ProcessedData addMeta(String key, Object value) {
        this.metadata.put(key, value);
        return this;
    }
}
