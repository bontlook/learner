package com.learner.agent.server.controller;

import com.learner.agent.model.ModelManager;
import com.learner.agent.server.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 模型管理控制器
 */
@RestController
@RequestMapping("/api/v1/models")
public class ModelController {

    private static final Logger log = LoggerFactory.getLogger(ModelController.class);

    private final ModelManager modelManager;

    public ModelController(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    /**
     * 列出所有注册模型
     * <pre>
     * GET /api/v1/models
     * </pre>
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> listModels() {
        List<Map<String, String>> models = modelManager.getAllModels().values().stream()
                .map(info -> Map.of(
                        "name", info.getName(),
                        "description", info.getDescription(),
                        "type", info.getType(),
                        "status", info.getStatus()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.ok(models));
    }

    /**
     * 获取单个模型信息
     * <pre>
     * GET /api/v1/models/{name}
     * </pre>
     */
    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<Map<String, String>>> getModel(@PathVariable String name) {
        ModelManager.ModelInfo info = modelManager.getModelInfo(name);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, String> data = Map.of(
                "name", info.getName(),
                "description", info.getDescription(),
                "type", info.getType(),
                "status", info.getStatus()
        );

        return ResponseEntity.ok(ApiResponse.ok(data));
    }

    /**
     * 加载模型
     * <pre>
     * POST /api/v1/models/load
     * { "name": "t5-small" }
     * </pre>
     */
    @PostMapping("/load")
    public ResponseEntity<ApiResponse<String>> loadModel(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("model name 不能为空"));
        }

        log.info("加载模型: {}", name);
        try {
            modelManager.loadPredictor(name, 1024, 150);
            return ResponseEntity.ok(ApiResponse.ok("模型 " + name + " 加载完成"));
        } catch (Exception e) {
            log.error("加载模型失败: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("加载失败: " + e.getMessage()));
        }
    }
}
