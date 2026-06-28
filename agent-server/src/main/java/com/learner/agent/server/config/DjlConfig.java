package com.learner.agent.server.config;

import com.learner.agent.model.ModelManager;
import com.learner.agent.model.TransformerSummarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * DJL 模型配置 — Spring Bean 定义
 * <p>
 * 管理：
 * - ModelManager Bean（全局单例，管理所有模型）
 * - TransformerSummarizer Bean（默认总结器实例）
 */
@Configuration
public class DjlConfig {

    private static final Logger log = LoggerFactory.getLogger(DjlConfig.class);

    @Value("${agent.model-dir:./models}")
    private String modelDir;

    @Value("${agent.default-model:distilbart-cnn-6-12}")
    private String defaultModel;

    @Value("${agent.max-summary-length:150}")
    private int maxSummaryLength;

    @Value("${agent.chunk-size:1024}")
    private int chunkSize;

    /**
     * 全局模型管理器
     */
    @Bean(destroyMethod = "close")
    public ModelManager modelManager() {
        Path path = Paths.get(modelDir);
        ModelManager manager = new ModelManager(path);

        // 注册已知可用的总结模型
        manager.register("distilbart-cnn-6-12",
                "DistilBART-CNN — Facebook 的轻量摘要模型（~300MB）",
                ModelManager.ModelType.SEQ2SEQ);

        manager.register("t5-small",
                "T5-Small — Google 的通用文本生成模型（~240MB）",
                ModelManager.ModelType.SEQ2SEQ);

        manager.register("qwen2.5-1.5b",
                "Qwen2.5-1.5B — 阿里通义千问小模型（~3GB，中文友好）",
                ModelManager.ModelType.DECODER_ONLY);

        log.info("模型管理器初始化完成，模型目录: {}", path.toAbsolutePath());
        return manager;
    }

    /**
     * 默认总结器 — 使用默认模型
     */
    @Bean
    public TransformerSummarizer transformerSummarizer(ModelManager modelManager) {
        log.info("创建默认总结器: {} (maxInput={}, maxOutput={})",
                defaultModel, chunkSize, maxSummaryLength);
        return new TransformerSummarizer(modelManager, defaultModel, chunkSize, maxSummaryLength);
    }
}
