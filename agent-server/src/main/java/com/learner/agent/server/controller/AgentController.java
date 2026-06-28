package com.learner.agent.server.controller;

import com.learner.agent.core.Task;
import com.learner.agent.core.TaskResult;
import com.learner.agent.server.dto.ApiResponse;
import com.learner.agent.server.dto.ProcessRequest;
import com.learner.agent.server.dto.SummarizeRequest;
import com.learner.agent.core.Agent;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Agent 主控制器 — 数据处理 + 总结
 */
@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {

    private static final Logger log = LoggerFactory.getLogger(AgentController.class);

    private final Agent agent;

    public AgentController(Agent agent) {
        this.agent = agent;
    }

    /**
     * 文本总结（同步，最常用）
     * <pre>
     * POST /api/v1/agent/summarize
     * {
     *   "text": "要总结的长文本...",
     *   "maxLength": 150,
     *   "minLength": 40
     * }
     * </pre>
     */
    @PostMapping("/summarize")
    public ResponseEntity<ApiResponse<String>> summarize(@Valid @RequestBody SummarizeRequest request) {
        log.info("收到总结请求: text length={}, maxLen={}",
                request.getText().length(), request.getMaxLength());

        String summary = agent.quickSummarize(
                request.getText(),
                request.getMaxLength(),
                request.getMinLength()
        );

        return ResponseEntity.ok(ApiResponse.ok("总结完成", summary));
    }

    /**
     * 完整数据处理任务（异步）
     * <pre>
     * POST /api/v1/agent/process
     * {
     *   "type": "text",
     *   "params": { "text": "..." },
     *   "pipeline": "default",
     *   "maxLength": 150
     * }
     * </pre>
     */
    @PostMapping("/process")
    public ResponseEntity<ApiResponse<Task>> process(@Valid @RequestBody ProcessRequest request) {
        Task task = new Task();
        task.setSourceType(request.getType());
        task.setSourceParams(request.getParams());
        task.setPipelineName(request.getPipeline());
        task.getSummarizeOptions().setMaxLength(request.getMaxLength());
        task.getSummarizeOptions().setMinLength(request.getMinLength());
        if (request.getModel() != null) {
            task.setModelName(request.getModel());
        }

        log.info("提交任务: id={}, type={}", task.getId(), task.getType());

        // 异步执行
        agent.executeAsync(task);

        return ResponseEntity.accepted()
                .body(ApiResponse.ok("任务已提交", task));
    }

    /**
     * 查询任务状态
     * <pre>
     * GET /api/v1/agent/task/{id}
     * </pre>
     */
    @GetMapping("/task/{id}")
    public ResponseEntity<ApiResponse<TaskResult>> getTask(@PathVariable String id) {
        TaskResult result = agent.getTaskStatus(id);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        if (result.getStatus() == Task.TaskStatus.FAILED) {
            return ResponseEntity.ok(ApiResponse.error(result.getError()));
        }

        return ResponseEntity.ok(ApiResponse.ok(result));
    }
}
