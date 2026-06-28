package com.learner.agent.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 总结请求 DTO
 */
public class SummarizeRequest {

    @NotBlank(message = "text 不能为空")
    @Size(max = 50000, message = "文本长度不能超过 50000 字符")
    private String text;

    /** 总结最大长度（token） */
    private int maxLength = 150;

    /** 总结最小长度（token） */
    private int minLength = 40;

    /** 是否使用抽取式总结 */
    private boolean extractive = false;

    /** 使用的模型名称 */
    private String model;

    // ===== Getters & Setters =====

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }

    public boolean isExtractive() { return extractive; }
    public void setExtractive(boolean extractive) { this.extractive = extractive; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
