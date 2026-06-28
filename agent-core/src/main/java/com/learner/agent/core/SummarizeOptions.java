package com.learner.agent.core;

import java.util.List;

/**
 * 总结选项
 */
public class SummarizeOptions {

    /** 总结最大长度（token） */
    private int maxLength = 150;
    /** 总结最小长度（token） */
    private int minLength = 40;
    /** 温度（控制随机性，0=确定性） */
    private double temperature = 0.3;
    /** 是否做抽取式总结（false = 生成式） */
    private boolean extractive = false;

    public SummarizeOptions() {}

    public SummarizeOptions(int maxLength, int minLength) {
        this.maxLength = maxLength;
        this.minLength = minLength;
    }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }

    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }

    public boolean isExtractive() { return extractive; }
    public void setExtractive(boolean extractive) { this.extractive = extractive; }
}
