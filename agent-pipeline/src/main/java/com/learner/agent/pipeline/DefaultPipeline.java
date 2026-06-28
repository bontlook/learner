package com.learner.agent.pipeline;

import com.learner.agent.core.Pipeline;
import com.learner.agent.core.ProcessedData;
import com.learner.agent.core.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认流水线实现 — 按顺序串联执行所有处理器
 */
public class DefaultPipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(DefaultPipeline.class);

    private final String name;
    private final List<Processor> processors = new ArrayList<>();

    public DefaultPipeline(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ProcessedData execute(ProcessedData input, Map<String, Object> config) {
        ProcessedData current = input;
        for (Processor processor : processors) {
            long start = System.currentTimeMillis();
            try {
                current = processor.process(current, config);
                log.debug("[{}] {} 完成 ({}ms)", name, processor.getName(),
                        System.currentTimeMillis() - start);
            } catch (Exception e) {
                log.error("[{}] {} 失败: {}", name, processor.getName(), e.getMessage());
                throw new RuntimeException("流水线 [" + name + "] 在 " + processor.getName() + " 处失败", e);
            }
        }
        return current;
    }

    @Override
    public DefaultPipeline addProcessor(Processor processor) {
        this.processors.add(processor);
        return this;
    }

    @Override
    public List<Processor> getProcessors() {
        return List.copyOf(processors);
    }
}
