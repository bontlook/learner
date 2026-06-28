package com.learner.agent.model;

import ai.djl.Model;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import com.learner.agent.model.translators.SummarizationTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型管理器 — 负责模型加载、缓存和生命周期管理
 * <p>
 * 支持：
 * - 从本地路径加载 ONNX 模型
 * - 模型缓存（avoid reloading）
 * - 模型状态查询
 */
public class ModelManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ModelManager.class);

    /** 注册的模型信息 */
    private final Map<String, ModelInfo> modelRegistry = new ConcurrentHashMap<>();

    /** 模型存储根目录 */
    private final Path modelDir;

    public ModelManager(Path modelDir) {
        this.modelDir = modelDir;
    }

    /**
     * 注册模型（不加载）
     */
    public void register(String name, String description, ModelType type) {
        modelRegistry.put(name, new ModelInfo(name, description, type, ModelStatus.REGISTERED));
        log.info("注册模型: {} ({})", name, type);
    }

    /**
     * 加载模型并返回 Predictor
     */
    public ai.djl.inference.Predictor<String, String> loadPredictor(String modelName,
                                                                      int maxInputLen,
                                                                      int maxOutputLen) {
        ModelInfo info = modelRegistry.get(modelName);
        if (info == null) {
            throw new IllegalArgumentException("未注册的模型: " + modelName);
        }

        try {
            Path modelPath = modelDir.resolve(modelName);
            if (!java.nio.file.Files.exists(modelPath)) {
                throw new IllegalStateException(
                    "模型文件不存在: " + modelPath + "\n请将模型文件放入此目录后重试。\n" +
                    "支持的格式: .onnx (ONNX Runtime)\n" +
                    "可以从 HuggingFace 下载并转换为 ONNX 格式。");
            }

            info.status = ModelStatus.LOADING;

            // 使用 DJL Criteria 加载本地模型
            Criteria<String, String> criteria = Criteria.builder()
                    .setTypes(String.class, String.class)
                    .optModelPath(modelPath)
                    .optTranslator(new SummarizationTranslator(maxInputLen, maxOutputLen, info.type.name()))
                    .optEngine("OnnxRuntime")
                    .optOption("mapLocation", "true")  // 允许 CPU 加载 GPU 训练的模型
                    .build();

            ZooModel<String, String> model = criteria.loadModel();
            ai.djl.inference.Predictor<String, String> predictor = model.newPredictor();

            info.model = model;
            info.predictor = predictor;
            info.status = ModelStatus.READY;

            log.info("模型 {} 加载完成", modelName);
            return predictor;

        } catch (Exception e) {
            info.status = ModelStatus.ERROR;
            log.error("模型 {} 加载失败: {}", modelName, e.getMessage());
            throw new RuntimeException("加载模型失败: " + modelName, e);
        }
    }

    /**
     * 获取模型信息
     */
    public ModelInfo getModelInfo(String name) {
        return modelRegistry.get(name);
    }

    /**
     * 获取所有已注册模型
     */
    public Map<String, ModelInfo> getAllModels() {
        return Map.copyOf(modelRegistry);
    }

    /**
     * 检查模型是否就绪
     */
    public boolean isReady(String name) {
        ModelInfo info = modelRegistry.get(name);
        return info != null && info.status == ModelStatus.READY;
    }

    @Override
    public void close() {
        modelRegistry.values().forEach(info -> {
            if (info.predictor != null) {
                info.predictor.close();
            }
            if (info.model != null) {
                info.model.close();
            }
        });
        modelRegistry.clear();
        log.info("所有模型已释放");
    }

    // ===== 内部类型 =====

    public enum ModelType {
        /** Transformer 编码器-解码器（BART, T5） */
        SEQ2SEQ,
        /** 仅解码器（GPT, LLaMA） */
        DECODER_ONLY,
        /** 自定义 */
        CUSTOM
    }

    public enum ModelStatus {
        REGISTERED, LOADING, READY, ERROR
    }

    public static class ModelInfo {
        public final String name;
        public final String description;
        public final ModelType type;
        public volatile ModelStatus status;
        public volatile ZooModel<String, String> model;
        public volatile ai.djl.inference.Predictor<String, String> predictor;

        public ModelInfo(String name, String description, ModelType type, ModelStatus status) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.status = status;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getType() { return type.name(); }
        public String getStatus() { return status.name(); }
    }
}
