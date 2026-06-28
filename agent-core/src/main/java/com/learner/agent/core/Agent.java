package com.learner.agent.core;

import java.util.concurrent.CompletableFuture;

/**
 * Agent 主接口 — 整个系统的入口
 * <p>
 * 负责协调 DataSource → Pipeline → Summarizer 的完整流程。
 * 支持同步和异步两种执行模式。
 */
public interface Agent {

    /** 同步执行任务 */
    TaskResult execute(Task task);

    /** 异步执行任务 */
    CompletableFuture<TaskResult> executeAsync(Task task);

    /** 查询任务状态 */
    TaskResult getTaskStatus(String taskId);

    /** 取消任务 */
    boolean cancelTask(String taskId);

    /** 快速总结（同步，不走完整流水线） */
    String quickSummarize(String text, int maxLength, int minLength);
}
