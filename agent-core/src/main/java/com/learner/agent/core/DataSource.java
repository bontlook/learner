package com.learner.agent.core;

import java.util.Map;

/**
 * 数据源接口 — 统一不同来源的数据接入
 *
 * @param <T> 数据源返回的原始数据类型
 */
public interface DataSource<T> {

    /** 数据源名称，如 "text"、"json"、"csv" */
    String getName();

    /** 是否支持指定的数据类型 */
    boolean supports(String type);

    /** 从参数中获取数据 */
    T fetch(Map<String, Object> params);
}
