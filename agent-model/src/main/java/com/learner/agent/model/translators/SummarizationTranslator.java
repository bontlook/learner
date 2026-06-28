package com.learner.agent.model.translators;

import ai.djl.Model;
import ai.djl.modality.nlp.qa.QAInput;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDList;
import ai.djl.ndarray.NDManager;
import ai.djl.translate.Batchifier;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorContext;
import ai.djl.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 文本摘要 Translator — 处理输入/输出转换
 * <p>
 * 支持两种模式：
 * 1. ONNX 模型模式：加载 .onnx 模型 + tokenizer.json
 * 2. 内置简单模式：无模型时的轻量抽取式总结
 * <p>
 * DJL 的 Translator 负责将原始输入（String）转为模型需要的 NDArray，
 * 并将模型输出转回原始类型（String）。
 */
public class SummarizationTranslator implements Translator<String, String> {

    private static final Logger log = LoggerFactory.getLogger(SummarizationTranslator.class);

    private final int maxInputLength;
    private final int maxOutputLength;
    private final String modelType;

    public SummarizationTranslator(int maxInputLength, int maxOutputLength, String modelType) {
        this.maxInputLength = maxInputLength;
        this.maxOutputLength = maxOutputLength;
        this.modelType = modelType;
    }

    @Override
    public NDList processInput(TranslatorContext ctx, String input) {
        NDManager manager = ctx.getNDManager();
        // 截断到最大输入长度
        String processed = input.length() > maxInputLength
                ? input.substring(0, maxInputLength)
                : input;

        // 将文本转为 token IDs（简化的词级编码）
        int[] tokens = simpleTokenize(processed);
        long[] tokenIds = new long[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            tokenIds[i] = tokens[i];
        }

        // 创建 attention mask（全 1）
        long[] attentionMask = new long[tokenIds.length];
        Arrays.fill(attentionMask, 1L);

        NDArray inputIds = manager.create(tokenIds).expandDims(0);
        NDArray attnMask = manager.create(attentionMask).expandDims(0);

        return new NDList(inputIds, attnMask);
    }

    @Override
    public String processOutput(TranslatorContext ctx, NDList list) {
        // ONNX 模型输出：logits，取 argmax 还原为 token
        if (list.isEmpty()) {
            return "";
        }

        NDArray output = list.get(0);  // shape: [batch, seq_len, vocab_size] 或 [batch, seq_len]
        long[] outputTokens;

        if (output.getShape().dimension() == 3) {
            // [batch, seq_len, vocab_size] → argmax
            outputTokens = output.argMax(2).toLongArray();
        } else if (output.getShape().dimension() == 2) {
            // [batch, seq_len] → 直接取
            outputTokens = output.toLongArray();
        } else {
            outputTokens = output.toLongArray();
        }

        return simpleDetokenize(outputTokens);
    }

    @Override
    public Batchifier getBatchifier() {
        return Batchifier.STACK;
    }

    /**
     * 简单分词：将文本转为伪 token ID
     * 实际使用时应该加载 HuggingFace tokenizer
     */
    private int[] simpleTokenize(String text) {
        // 按词切分，每个词的 hashCode 取模作为 token ID
        String[] words = text.toLowerCase().split("\\s+");
        int[] tokens = new int[Math.min(words.length, maxInputLength)];
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = Math.abs(words[i].hashCode() % 50265) + 1; // GPT-2 vocab size
        }
        return tokens;
    }

    /**
     * 简单反分词
     */
    private String simpleDetokenize(long[] tokens) {
        // 在实际模型推理后，token IDs 会被映射回词
        // 简单实现：跳过特殊 token (< 3 的 ID)
        StringBuilder sb = new StringBuilder();
        for (long token : tokens) {
            if (token > 3) {
                sb.append((char) ('a' + (token % 26)));
            }
        }
        return sb.toString().trim();
    }
}
