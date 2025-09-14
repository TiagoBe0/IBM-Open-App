package com.sbs.open_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para respuestas del servicio de IA local
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AIResponse {
    
    private boolean success;
    private String content;
    private String error;
    private String model;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private Double processingTime;
    private Map<String, Object> metadata;
    
    // Constructores
    private AIResponse() {}
    
    // Factory methods para éxito
    public static AIResponse success(String content, String model) {
        AIResponse response = new AIResponse();
        response.success = true;
        response.content = content;
        response.model = model;
        return response;
    }
    
    public static AIResponse success(String content, String model, 
                                   int promptTokens, int completionTokens) {
        AIResponse response = success(content, model);
        response.promptTokens = promptTokens;
        response.completionTokens = completionTokens;
        response.totalTokens = promptTokens + completionTokens;
        return response;
    }
    
    // Factory methods para error
    public static AIResponse error(String error) {
        AIResponse response = new AIResponse();
        response.success = false;
        response.error = error;
        return response;
    }
    
    public static AIResponse error(String error, String model) {
        AIResponse response = error(error);
        response.model = model;
        return response;
    }
    
    // Builder-style methods
    public AIResponse withTokens(int promptTokens, int completionTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = promptTokens + completionTokens;
        return this;
    }
    
    public AIResponse withProcessingTime(double processingTime) {
        this.processingTime = processingTime;
        return this;
    }
    
    public AIResponse withMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
        return this;
    }
    
    public AIResponse addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    // Getters
    public boolean isSuccess() { 
        return success; 
    }
    
    public String getContent() { 
        return content; 
    }
    
    public String getError() { 
        return error; 
    }
    
    public String getModel() { 
        return model; 
    }
    
    public Integer getPromptTokens() { 
        return promptTokens; 
    }
    
    public Integer getCompletionTokens() { 
        return completionTokens; 
    }
    
    public Integer getTotalTokens() { 
        return totalTokens; 
    }
    
    public Double getProcessingTime() { 
        return processingTime; 
    }
    
    public Map<String, Object> getMetadata() { 
        return metadata; 
    }
    
    @Override
    public String toString() {
        return "AIResponse{" +
                "success=" + success +
                ", content='" + content + '\'' +
                ", error='" + error + '\'' +
                ", model='" + model + '\'' +
                ", promptTokens=" + promptTokens +
                ", completionTokens=" + completionTokens +
                ", totalTokens=" + totalTokens +
                ", processingTime=" + processingTime +
                ", metadata=" + metadata +
                '}';
    }
}