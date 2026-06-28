package com.learner.agent.pipeline;

import com.learner.agent.core.ProcessedData;
import com.learner.agent.core.Processor;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 关键词提取处理器 — 基于 TF 的简单关键词提取
 * <p>
 * 注意：这是轻量级实现，不依赖外部 NLP 库。
 * 如果需要更精确的关键词提取，可替换为 HanLP 或 Stanford CoreNLP。
 */
public class ExtractProcessor implements Processor {

    /** 停用词正则 */
    private static final Pattern STOP_WORDS = Pattern.compile(
            "^(的|了|在|是|我|你|他|她|它|们|这|那|和|与|或|但|而|也|就|都|还|要|有|会|能|可以|" +
            "the|a|an|is|are|was|were|be|been|being|have|has|had|do|does|did|will|would|shall|" +
            "should|may|might|must|can|could|to|of|in|for|on|with|at|by|from|as|into|through|" +
            "during|before|after|above|below|between|and|but|or|nor|not|so|if|then|than|too|very|" +
            "just|about|each|all|both|few|more|most|other|some|such|only|own|same|new|its|it)$",
            Pattern.CASE_INSENSITIVE
    );

    @Override
    public String getName() {
        return "extract";
    }

    @Override
    public ProcessedData process(ProcessedData input, Map<String, Object> config) {
        String text = input.getText();
        if (text == null || text.isEmpty()) {
            input.addMeta("keywords", List.of());
            return input;
        }

        int topN = config != null && config.containsKey("topN")
                ? ((Number) config.get("topN")).intValue()
                : 10;

        // 分词：中文按字符级 bigram，英文按空格
        Map<String, Integer> tf = new HashMap<>();

        // 英文词
        String[] words = text.toLowerCase().split("[^a-zA-Z一-鿿]+");
        for (String word : words) {
            if (word.length() < 2) continue;
            if (STOP_WORDS.matcher(word).matches()) continue;
            tf.merge(word, 1, Integer::sum);
        }

        // 中文 bigram（对中文文本补充 bigram 特征）
        String chineseOnly = text.replaceAll("[^一-鿿]", "");
        for (int i = 0; i < chineseOnly.length() - 1; i++) {
            String bigram = chineseOnly.substring(i, i + 2);
            tf.merge("zh:" + bigram, 1, Integer::sum);
        }

        // 按频率排序取 topN
        List<String> keywords = tf.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(topN)
                .map(e -> e.getKey().replace("zh:", ""))
                .collect(Collectors.toList());

        input.addMeta("keywords", keywords);
        return input;
    }
}
