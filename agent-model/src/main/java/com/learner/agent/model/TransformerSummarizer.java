package com.learner.agent.model;

import cn.hutool.core.lang.hash.Hash;
import com.learner.agent.core.SummarizeOptions;
import com.learner.agent.core.Summarizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Transformer 总结器 — 基于 DJL 的文本摘要实现
 * <p>
 * 支持两种模式：
 * 1. DJL 模型模式（需要下载 ONNX 模型）
 * 2. 抽取式后备模式（无需模型，开箱即用）
 * <p>
 * 模型未就绪时自动降级到抽取式总结，确保服务始终可用。
 */
public class TransformerSummarizer implements Summarizer {

    private static final Logger log = LoggerFactory.getLogger(TransformerSummarizer.class);

    private final ModelManager modelManager;
    private final String modelName;
    private final int maxInputLength;
    private final int maxOutputLength;

    /** 模型 Predictor 缓存 */
    private volatile ai.djl.inference.Predictor<String, String> predictor;
    private volatile boolean modelTried = false;

    public TransformerSummarizer(ModelManager modelManager, String modelName,
                                  int maxInputLength, int maxOutputLength) {
        this.modelManager = modelManager;
        this.modelName = modelName;
        this.maxInputLength = maxInputLength;
        this.maxOutputLength = maxOutputLength;
    }

    @Override
    public String summarize(String text, SummarizeOptions options) {
        if (text == null || text.isBlank()) {
            return "";
        }

        // 如果模型已就绪，使用 DJL 模型推理
        if (modelManager.isReady(modelName)) {
            try {
                return summarizeWithModel(text, options);
            } catch (Exception e) {
                log.warn("模型推理失败，回退到抽取式总结: {}", e.getMessage());
            }
        }

        // 后备：抽取式总结
        return extractiveSummarize(text, options);
    }

    @Override
    public List<String> summarizeBatch(List<String> texts, SummarizeOptions options) {
        return texts.stream()
                .map(t -> summarize(t, options))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isReady() {
        return modelManager.isReady(modelName);
    }

    /**
     * 使用 DJL 模型做生成式总结
     */
    private String summarizeWithModel(String text, SummarizeOptions options) {
        if (predictor == null) {
            synchronized (this) {
                if (predictor == null && !modelTried) {
                    modelTried = true;
                    try {
                        predictor = modelManager.loadPredictor(modelName, maxInputLength, maxOutputLength);
                    } catch (Exception e) {
                        log.warn("模型加载失败，将使用抽取式总结: {}", e.getMessage());
                        return extractiveSummarize(text, options);
                    }
                }
            }
        }

        if (predictor == null) {
            return extractiveSummarize(text, options);
        }

        // 添加总结提示词
        String prompt = "summarize: " + text;
        String result;
        try {
            result = predictor.predict(prompt);
        } catch (ai.djl.translate.TranslateException e) {
            log.warn("模型推理异常，回退到抽取式总结: {}", e.getMessage());
            return extractiveSummarize(text, options);
        }

        // 清理输出
        return cleanOutput(result, options.getMaxLength());
    }

    /**
     * 抽取式总结 — 无需模型，基于句子打分选择关键句
     * <p>
     * 算法：TF-ISF（词频-逆句频），选择得分最高的句子组成摘要。
     */
    private String extractiveSummarize(String text, SummarizeOptions options) {
        // 1. 分句
        List<String> sentences = splitSentences(text);
        if (sentences.size() <= 3) {
            return text; // 太短，直接返回
        }

        // 2. 计算词频（TF）
        Map<String, Integer> tf = new HashMap<>();
        List<Map<String, Integer>> sentenceWordCounts = new ArrayList<>();

        for (String sentence : sentences) {
            Map<String, Integer> wordCount = new HashMap<>();
            String[] words = tokenize(sentence);
            for (String word : words) {
                if (isStopWord(word)) continue;
                tf.merge(word, 1, Integer::sum);
                wordCount.merge(word, 1, Integer::sum);
            }
            sentenceWordCounts.add(wordCount);
        }

        int totalSentences = sentences.size();

        // 3. 计算每个句子的 TF-ISF 得分
        List<Double> scores = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            Map<String, Integer> wordCount = sentenceWordCounts.get(i);
            double score = 0.0;
            int wordTotal = wordCount.values().stream().mapToInt(Integer::intValue).sum();
            if (wordTotal == 0) { scores.add(0.0); continue; }

            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                String word = entry.getKey();
                int count = entry.getValue();
                int df = tf.getOrDefault(word, 1);
                double isf = Math.log((double) totalSentences / df);
                score += ((double) count / wordTotal) * isf;
            }

            // 位置加分：开头和结尾的句子更重要
            if (i == 0) score *= 1.5;
            if (i == sentences.size() - 1) score *= 1.3;

            scores.add(score);
        }

        // 4. 选择得分最高的句子
        int targetCount = Math.min(
                Math.max(2, sentences.size() / 3),  // 保留约 1/3 的句子
                Math.min(options.getMaxLength() / 20, 8) // 但不超过 maxLength 限制
        );

        // 按得分排序，取 top-K
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) indices.add(i);

        indices.sort((a, b) -> Double.compare(scores.get(b), scores.get(a)));

        List<Integer> selected = indices.subList(0, Math.min(targetCount, indices.size()));
        selected.sort(Integer::compareTo); // 恢复原文顺序

        // 5. 拼接摘要
        return selected.stream()
                .map(sentences::get)
                .collect(Collectors.joining(" "));
    }

    /** 分句 — 中英文兼容 */
    private List<String> splitSentences(String text) {
        List<String> sentences = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            current.append(c);

            // 句子结束标记
            if (c == '.' || c == '!' || c == '?' ||
                c == '。' || c == '！' || c == '？' ||
                c == '\n') {
                String sentence = current.toString().trim();
                if (sentence.length() > 3) {
                    sentences.add(sentence);
                }
                current = new StringBuilder();
            }
        }

        // 最后一句
        String last = current.toString().trim();
        if (last.length() > 3) {
            sentences.add(last);
        }

        return sentences;
    }

    /** 分词 */
    private String[] tokenize(String sentence) {
        return sentence.toLowerCase()
                .replaceAll("[^a-zA-Z0-9一-鿿]", " ")
                .split("\\s+");
    }

    /** 停用词判断 */
    private boolean isStopWord(String word) {
        if (word.length() < 2) return true;
        Set<String> stopWords = new java.util.HashSet<>(java.util.Arrays.asList(
            "the", "a", "an", "is", "are", "was", "were", "be", "been",
            "being", "have", "has", "had", "do", "does", "did", "will",
            "would", "shall", "should", "may", "might", "must", "can",
            "could", "to", "of", "in", "for", "on", "with", "at", "by",
            "from", "as", "into", "through", "during", "before", "after",
            "above", "below", "between", "and", "but", "or", "nor", "not",
            "so", "if", "then", "than", "too", "very", "just", "about",
            "each", "all", "both", "few", "more", "most", "other", "some",
            "such", "no", "only", "own", "same", "that",
            "的", "了", "在", "是", "我", "你", "他", "她", "它", "们",
            "这", "那", "和", "与", "或", "但", "而", "也", "就", "都",
            "还", "要", "有", "会", "能", "可以", "很", "非常",
            "把", "被", "让", "从", "到", "对", "向", "跟", "同", "比"
        ));
        return stopWords.contains(word);
    }

    /** 清理模型输出 */
    private String cleanOutput(String output, int maxLength) {
        if (output == null || output.isBlank()) return "";
        // 去除重复的空格和换行
        output = output.replaceAll("\\s{2,}", " ").trim();
        // 截断
        if (output.length() > maxLength * 4) { // 粗略估计 4 字符 ≈ 1 token
            output = output.substring(0, maxLength * 4);
        }
        return output;
    }
}
