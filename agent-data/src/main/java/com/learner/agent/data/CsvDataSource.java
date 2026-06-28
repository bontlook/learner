package com.learner.agent.data;

import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import com.learner.agent.core.DataSource;
import com.learner.agent.core.ProcessedData;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CSV 数据源 — 解析 CSV 并以结构化文本呈现
 */
public class CsvDataSource implements DataSource<ProcessedData> {

    @Override
    public String getName() {
        return "csv";
    }

    @Override
    public boolean supports(String type) {
        return "csv".equalsIgnoreCase(type);
    }

    @Override
    public ProcessedData fetch(Map<String, Object> params) {
        String csvStr = (String) params.get("csv");
        if (csvStr == null || csvStr.isBlank()) {
            throw new IllegalArgumentException("csv 参数不能为空");
        }

        String source = (String) params.getOrDefault("source", "csv-input");
        String id = "csv-" + System.currentTimeMillis();

        // 解析 CSV
        CsvReader reader = CsvUtil.getReader();
        CsvData csvData = reader.read(new StringReader(csvStr));
        List<CsvRow> rows = csvData.getRows();

        if (rows.isEmpty()) {
            return new ProcessedData(id, "", source, "csv");
        }

        // CSV 转结构化文本：带表头的表格形式
        List<String> headers = rows.get(0).getRawList();
        List<String> lines = new ArrayList<>();

        // 表头行
        lines.add("| " + String.join(" | ", headers) + " |");
        lines.add("|" + headers.stream().map(h -> "---").collect(Collectors.joining("|")) + "|");

        // 数据行
        for (int i = 1; i < Math.min(rows.size(), 51); i++) { // 最多 50 行数据
            List<String> cells = rows.get(i).getRawList();
            lines.add("| " + String.join(" | ", cells) + " |");
        }

        String markdown = String.join("\n", lines);

        ProcessedData data = new ProcessedData(id, markdown, source, "csv");
        data.addMeta("rowCount", rows.size() - 1);
        data.addMeta("columns", headers);
        return data;
    }
}
