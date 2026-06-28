package com.learner.agent.data;

import cn.hutool.http.HttpUtil;
import com.learner.agent.core.DataSource;
import com.learner.agent.core.ProcessedData;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

/**
 * 网页数据源 — 抓取 URL 内容并提取正文
 */
public class WebDataSource implements DataSource<ProcessedData> {

    @Override
    public String getName() {
        return "web";
    }

    @Override
    public boolean supports(String type) {
        return "web".equalsIgnoreCase(type) || "url".equalsIgnoreCase(type) || "html".equalsIgnoreCase(type);
    }

    @Override
    public ProcessedData fetch(Map<String, Object> params) {
        String url = (String) params.get("url");
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url 参数不能为空");
        }

        String id = "web-" + System.currentTimeMillis();

        // 抓取网页
        String html = HttpUtil.get(url, 10000);

        // 用 Jsoup 解析，提取正文
        Document doc = Jsoup.parse(html);

        // 移除无用标签
        doc.select("script, style, nav, footer, header, aside, iframe, noscript").remove();

        String title = doc.title();
        String bodyText = doc.body().text();

        // 清理多余空白
        bodyText = bodyText.replaceAll("\\s{2,}", " ").trim();

        // 限制长度，避免 token 超限
        int maxLen = (int) params.getOrDefault("maxLength", 10000);
        if (bodyText.length() > maxLen) {
            bodyText = bodyText.substring(0, maxLen) + "...";
        }

        ProcessedData data = new ProcessedData(id, bodyText, url, "web");
        data.addMeta("title", title);
        data.addMeta("url", url);
        data.addMeta("originalLength", bodyText.length());
        return data;
    }
}
