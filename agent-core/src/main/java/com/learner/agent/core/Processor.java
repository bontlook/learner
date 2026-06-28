package com.learner.agent.core;

import java.util.Map;

/**
 * 处理器接口 — 流水线中的一环
 * <p>
 * 每个 Processor 对 ProcessedData 做一次变换，
 * 多个 Processor 串联组成 Pipeline。
 */
public interface Processor {

    /** 处理器名称，如 "clean"、"chunk"、"aggregate" */
    String getName();

    /** 处理数据，返回变换后的结果 */
    ProcessedData process(ProcessedData input, Map<String, Object> config);
}
