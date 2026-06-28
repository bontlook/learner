package com.learner.agent.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Java Agent — 数据处理 + Transformer 总结服务
 * <p>
 * 核心能力：
 * - 多数据源接入（文本、JSON、CSV、网页）
 * - 可配置的处理流水线
 * - 基于 DJL 的 Transformer 模型推理
 * - REST API + 异步任务管理
 */
@SpringBootApplication(scanBasePackages = "com.learner.agent")
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
