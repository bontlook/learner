package com.learner.agent.core;

import java.util.Map;

/**
 * 流水线接口 — 将一组 Processor 串联执行
 */
public interface Pipeline {

    /** 流水线名称 */
    String getName();

    /** 按顺序执行所有处理器 */
    ProcessedData execute(ProcessedData input, Map<String, Object> config);

    /** 向流水线添加处理器 */
    Pipeline addProcessor(Processor processor);

    /** 获取流水线中的所有处理器 */
    java.util.List<Processor> getProcessors();
}
