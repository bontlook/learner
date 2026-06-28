package com.learner.agent.pipeline;

import com.learner.agent.core.ProcessedData;
import com.learner.agent.core.Processor;

import java.util.List;
import java.util.Map;

/**
 * 结果聚合处理器 — 将分块处理后的结果合并
 */
public class AggregateProcessor implements Processor {

    @Override
    public String getName() {
        return "aggregate";
    }

    @Override
    public ProcessedData process(ProcessedData input, Map<String, Object> config) {
        List<String> chunkSummaries = (List<String>) input.getMetadata().get("chunkSummaries");

        if (chunkSummaries == null || chunkSummaries.isEmpty()) {
            // 没有分块总结，不做聚合
            input.addMeta("aggregated", false);
            return input;
        }

        // 将各块的总结拼接为完整总结
        String fullSummary = String.join("\n\n", chunkSummaries);
        input.addMeta("fullSummary", fullSummary);
        input.addMeta("aggregated", true);
        input.addMeta("chunkSummaryCount", chunkSummaries.size());

        return input;
    }
}
