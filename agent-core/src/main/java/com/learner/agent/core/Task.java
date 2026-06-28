package com.learner.agent.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 任务定义 — 描述一次数据处理+总结任务
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Task {

    public enum TaskType {
        /** 数据接入 + 处理 + 总结 */
        FULL_PIPELINE,
        /** 仅总结 */
        SUMMARIZE_ONLY,
        /** 仅数据处理 */
        PROCESS_ONLY
    }

    public enum TaskStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }

    /** 任务唯一 ID */
    private String id = UUID.randomUUID().toString().substring(0, 8);
    /** 任务类型 */
    private TaskType type = TaskType.FULL_PIPELINE;
    /** 任务状态 */
    private TaskStatus status = TaskStatus.PENDING;
    /** 数据源类型：text/json/csv/web */
    private String sourceType;
    /** 数据源参数 */
    private Map<String, Object> sourceParams;
    /** 总结选项 */
    private SummarizeOptions summarizeOptions = new SummarizeOptions();
    /** 使用的流水线名称 */
    private String pipelineName = "default";
    /** 使用的模型名称 */
    private String modelName;
    /** 创建时间 */
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== Getters & Setters =====

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public TaskType getType() { return type; }
    public void setType(TaskType type) { this.type = type; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public Map<String, Object> getSourceParams() { return sourceParams; }
    public void setSourceParams(Map<String, Object> sourceParams) { this.sourceParams = sourceParams; }

    public SummarizeOptions getSummarizeOptions() { return summarizeOptions; }
    public void setSummarizeOptions(SummarizeOptions summarizeOptions) { this.summarizeOptions = summarizeOptions; }

    public String getPipelineName() { return pipelineName; }
    public void setPipelineName(String pipelineName) { this.pipelineName = pipelineName; }

    public String getModelName() { return modelName; }
    public void setModelName(String modelName) { this.modelName = modelName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // ===== Fluent API =====

    public static Task ofText(String text) {
        Task t = new Task();
        t.sourceType = "text";
        t.sourceParams = Map.of("text", text);
        return t;
    }

    public static Task ofUrl(String url) {
        Task t = new Task();
        t.sourceType = "web";
        t.sourceParams = Map.of("url", url);
        return t;
    }
}
