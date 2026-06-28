package com.learner.agent.pipeline;

import com.learner.agent.core.ProcessedData;
import com.learner.agent.core.Processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文本分段处理器 — 将长文本分成适合 Transformer 模型处理的小块
 * <p>
 * 策略：优先按段落分，如果段落仍然太长，按句子分，
 * 如果句子还是太长，按固定字符数截断。
 */
public class ChunkProcessor implements Processor {

    /** 默认每段最大字符数 */
    private static final int DEFAULT_CHUNK_SIZE = 1024;

    @Override
    public String getName() {
        return "chunk";
    }

    @Override
    public ProcessedData process(ProcessedData input, Map<String, Object> config) {
        String text = input.getText();
        if (text == null || text.isEmpty()) {
            input.setChunks(List.of());
            return input;
        }

        int chunkSize = getChunkSize(config);
        List<String> chunks = new ArrayList<>();

        if (text.length() <= chunkSize) {
            // 文本足够短，不需要分段
            chunks.add(text);
        } else {
            // 先按段落分
            String[] paragraphs = text.split("\n\n");
            StringBuilder currentChunk = new StringBuilder();

            for (String para : paragraphs) {
                para = para.trim();
                if (para.isEmpty()) continue;

                // 如果当前 chunk + 新段落不超过限制，拼进去
                if (currentChunk.length() + para.length() + 1 <= chunkSize) {
                    if (currentChunk.length() > 0) currentChunk.append("\n\n");
                    currentChunk.append(para);
                } else {
                    // 保存当前 chunk，开新的
                    if (currentChunk.length() > 0) {
                        chunks.add(currentChunk.toString());
                    }
                    currentChunk = new StringBuilder();

                    // 如果单个段落仍然太长，按句子切
                    if (para.length() > chunkSize) {
                        chunks.addAll(splitBySentence(para, chunkSize));
                    } else {
                        currentChunk.append(para);
                    }
                }
            }

            // 最后一块
            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());
            }
        }

        input.setChunks(chunks);
        input.addMeta("chunkCount", chunks.size());
        input.addMeta("chunkSize", chunkSize);
        return input;
    }

    /** 按句子切分长段落 */
    private List<String> splitBySentence(String text, int chunkSize) {
        List<String> result = new ArrayList<>();
        String[] sentences = text.split("(?<=[。！？.!?])\\s*");
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            if (current.length() + sentence.length() <= chunkSize) {
                current.append(sentence);
            } else {
                if (current.length() > 0) {
                    result.add(current.toString());
                }
                // 如果单个句子也超长，硬截断
                if (sentence.length() > chunkSize) {
                    for (int i = 0; i < sentence.length(); i += chunkSize) {
                        int end = Math.min(i + chunkSize, sentence.length());
                        result.add(sentence.substring(i, end));
                    }
                } else {
                    current = new StringBuilder(sentence);
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.toString());
        }

        return result;
    }

    private int getChunkSize(Map<String, Object> config) {
        if (config != null && config.containsKey("chunkSize")) {
            return ((Number) config.get("chunkSize")).intValue();
        }
        return DEFAULT_CHUNK_SIZE;
    }
}
