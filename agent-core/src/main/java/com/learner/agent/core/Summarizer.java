package com.learner.agent.core;

import java.util.List;

/**
 * 总结器接口 — 调用 Transformer 模型生成摘要
 */
public interface Summarizer {

    /** 对单段文本生成摘要 */
    String summarize(String text, SummarizeOptions options);

    /** 批量总结 */
    List<String> summarizeBatch(List<String> texts, SummarizeOptions options);

    /** 当前模型是否已加载 */
    boolean isReady();
}
