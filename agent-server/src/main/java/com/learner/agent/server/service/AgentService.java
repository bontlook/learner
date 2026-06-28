package com.learner.agent.server.service;

import com.learner.agent.core.*;
import com.learner.agent.core.Task.TaskStatus;
import com.learner.agent.data.CsvDataSource;
import com.learner.agent.data.JsonDataSource;
import com.learner.agent.data.TextDataSource;
import com.learner.agent.data.WebDataSource;
import com.learner.agent.model.TransformerSummarizer;
import com.learner.agent.pipeline.CleanProcessor;
import com.learner.agent.pipeline.ChunkProcessor;
import com.learner.agent.pipeline.DefaultPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent 服务 — 业务编排层
 * <p>
 * 负责：
 * - 任务提交和状态跟踪
 * - 数据源选择
 * - 流水线执行
 * - 模型总结调用
 */
@Service
public class AgentService implements Agent {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    /** 任务结果缓存 */
    private final Map<String, TaskResult> taskStore = new ConcurrentHashMap<>();

    /** 任务状态缓存 */
    private final Map<String, Task> taskStates = new ConcurrentHashMap<>();

    private final TransformerSummarizer summarizer;
    private final List<DataSource<?>> dataSources;

    public AgentService(TransformerSummarizer summarizer) {
        this.summarizer = summarizer;

        // 注册数据源
        this.dataSources = List.of(
                new TextDataSource(),
                new JsonDataSource(),
                new CsvDataSource(),
                new WebDataSource()
        );
    }

    @Override
    public TaskResult execute(Task task) {
        task.setStatus(TaskStatus.RUNNING);
        taskStates.put(task.getId(), task);
        long start = System.currentTimeMillis();

        try {
            // 1. 获取数据
            ProcessedData data = fetchData(task);

            // 2. 执行流水线
            data = runPipeline(data, task.getPipelineName());

            // 3. 生成总结
            String summary = "";
            if (data.getChunks() != null && !data.getChunks().isEmpty()) {
                List<String> chunkSummaries = data.getChunks().stream()
                        .map(chunk -> summarizer.summarize(chunk, task.getSummarizeOptions()))
                        .toList();
                summary = String.join("\n\n", chunkSummaries);
            } else {
                summary = summarizer.summarize(data.getText(), task.getSummarizeOptions());
            }

            long duration = System.currentTimeMillis() - start;
            TaskResult result = TaskResult.success(
                    task.getId(), summary, data.getText(), duration,
                    task.getModelName() != null ? task.getModelName() : "default"
            );
            taskStore.put(task.getId(), result);
            task.setStatus(TaskStatus.COMPLETED);

            log.info("任务 {} 完成: {}ms", task.getId(), duration);
            return result;

        } catch (Exception e) {
            log.error("任务 {} 失败: {}", task.getId(), e.getMessage());
            TaskResult result = TaskResult.failed(task.getId(), e.getMessage());
            taskStore.put(task.getId(), result);
            task.setStatus(TaskStatus.FAILED);
            return result;
        }
    }

    @Override
    @Async("agentTaskExecutor")
    public CompletableFuture<TaskResult> executeAsync(Task task) {
        return CompletableFuture.completedFuture(execute(task));
    }

    @Override
    public TaskResult getTaskStatus(String taskId) {
        return taskStore.get(taskId);
    }

    @Override
    public boolean cancelTask(String taskId) {
        Task task = taskStates.get(taskId);
        if (task != null && task.getStatus() == TaskStatus.RUNNING) {
            task.setStatus(TaskStatus.CANCELLED);
            return true;
        }
        return false;
    }

    /**
     * 快速总结（同步，不走完整流水线）
     */
    public String quickSummarize(String text, int maxLength, int minLength) {
        SummarizeOptions options = new SummarizeOptions(maxLength, minLength);
        return summarizer.summarize(text, options);
    }

    /**
     * 根据任务选择数据源并获取数据
     */
    private ProcessedData fetchData(Task task) {
        String type = task.getSourceType();
        Map<String, Object> params = task.getSourceParams();

        DataSource<?> source = dataSources.stream()
                .filter(ds -> ds.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "不支持的数据类型: " + type + "。支持的类型: text, json, csv, web"));

        log.info("使用数据源: {} (type={})", source.getName(), type);
        return (ProcessedData) source.fetch(params);
    }

    /**
     * 构建并执行流水线
     */
    private ProcessedData runPipeline(ProcessedData data, String pipelineName) {
        DefaultPipeline pipeline = new DefaultPipeline(pipelineName != null ? pipelineName : "default");
        pipeline.addProcessor(new CleanProcessor())
                .addProcessor(new ChunkProcessor());

        Map<String, Object> config = Map.of("chunkSize", 1024);
        log.info("执行流水线: {}", pipelineName);
        return pipeline.execute(data, config);
    }
}
