package com.learner.agent.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * 任务执行结果
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResult {

    /** 关联的任务 ID */
    private String taskId;
    /** 任务状态 */
    private Task.TaskStatus status;
    /** 原始文本（处理后） */
    private String processedText;
    /** 总结结果 */
    private String summary;
    /** 处理耗时（毫秒） */
    private long durationMs;
    /** 错误信息（如果失败） */
    private String error;
    /** 使用的模型 */
    private String modelUsed;
    /** 完成时间 */
    private LocalDateTime completedAt = LocalDateTime.now();

    // ===== 工厂方法 =====

    public static TaskResult success(String taskId, String summary, String processedText,
                                      long durationMs, String modelUsed) {
        TaskResult r = new TaskResult();
        r.taskId = taskId;
        r.status = Task.TaskStatus.COMPLETED;
        r.summary = summary;
        r.processedText = processedText;
        r.durationMs = durationMs;
        r.modelUsed = modelUsed;
        return r;
    }

    public static TaskResult failed(String taskId, String error) {
        TaskResult r = new TaskResult();
        r.taskId = taskId;
        r.status = Task.TaskStatus.FAILED;
        r.error = error;
        return r;
    }

    // ===== Getters & Setters =====

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public Task.TaskStatus getStatus() { return status; }
    public void setStatus(Task.TaskStatus status) { this.status = status; }

    public String getProcessedText() { return processedText; }
    public void setProcessedText(String processedText) { this.processedText = processedText; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public long getDurationMs() { return durationMs; }
    public void setDurationMs(long durationMs) { this.durationMs = durationMs; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }
}
