package com.sbs.open_app.controllers;

import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
            "status", "ok",
            "service", "IA Local Debug",
            "timestamp", System.currentTimeMillis(),
            "endpoints", Map.of(
                "ia-status", "/api/local-ai/status",
                "ia-chat", "/api/local-ai/ask",
                "ia-models", "/api/local-ai/models"
            )
        );
    }
}