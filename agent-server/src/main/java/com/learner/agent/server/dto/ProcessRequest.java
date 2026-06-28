package com.learner.agent.server.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * 数据处理请求 DTO
 */
public class ProcessRequest {

    @NotBlank(message = "type 不能为空（text/json/csv/web）")
    private String type;

    /** 数据源参数 */
    private Map<String, Object> params;

    /** 流水线名称 */
    private String pipeline = "default";

    /** 总结选项 */
    private int maxLength = 150;
    private int minLength = 40;

    /** 使用的模型 */
    private String model;

    // ===== Getters & Setters =====

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Map<String, Object> getParams() { return params; }
    public void setParams(Map<String, Object> params) { this.params = params; }

    public String getPipeline() { return pipeline; }
    public void setPipeline(String pipeline) { this.pipeline = pipeline; }

    public int getMaxLength() { return maxLength; }
    public void setMaxLength(int maxLength) { this.maxLength = maxLength; }

    public int getMinLength() { return minLength; }
    public void setMinLength(int minLength) { this.minLength = minLength; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
