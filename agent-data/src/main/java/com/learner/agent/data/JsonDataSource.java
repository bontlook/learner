package com.learner.agent.data;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.learner.agent.core.DataSource;
import com.learner.agent.core.ProcessedData;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON 数据源 — 解析 JSON 并提取文本字段
 */
public class JsonDataSource implements DataSource<ProcessedData> {

    @Override
    public String getName() {
        return "json";
    }

    @Override
    public boolean supports(String type) {
        return "json".equalsIgnoreCase(type);
    }

    @Override
    public ProcessedData fetch(Map<String, Object> params) {
        String jsonStr = (String) params.get("json");
        if (jsonStr == null || jsonStr.isBlank()) {
            throw new IllegalArgumentException("json 参数不能为空");
        }

        String field = (String) params.getOrDefault("field", "");
        String source = (String) params.getOrDefault("source", "json-input");
        String id = "json-" + System.currentTimeMillis();

        // 尝试解析为 JSON 对象或数组，提取文本
        String extractedText;
        if (JSONUtil.isTypeJSONObject(jsonStr)) {
            JSONObject obj = JSONUtil.parseObj(jsonStr);
            if (!field.isEmpty() && obj.containsKey(field)) {
                extractedText = obj.getStr(field);
            } else {
                // 没有指定字段，将所有非空值拼起来
                extractedText = obj.entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .map(e -> e.getKey() + ": " + e.getValue().toString())
                        .collect(Collectors.joining("\n"));
            }
        } else if (JSONUtil.isTypeJSONArray(jsonStr)) {
            JSONArray arr = JSONUtil.parseArray(jsonStr);
            extractedText = arr.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining("\n"));
        } else {
            throw new IllegalArgumentException("无法解析为有效的 JSON");
        }

        return new ProcessedData(id, extractedText, source, "json");
    }
}
