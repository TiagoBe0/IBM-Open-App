package com.sbs.open_app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para el estado del modelo de IA local
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModelStatus {
    
    private boolean available;
    private String modelName;
    private String serverUrl;
    private String message;
    private String version;
    private Long modelSize;
    private Integer contextLength;
    private Map<String, Object> details;
    
    // Constructores
    private ModelStatus() {}
    
    // Factory methods para modelo disponible
    public static ModelStatus available(String modelName, String serverUrl) {
        ModelStatus status = new ModelStatus();
        status.available = true;
        status.modelName = modelName;
        status.serverUrl = serverUrl;
        status.message = "Modelo disponible y listo";
        return status;
    }
    
    public static ModelStatus available(String modelName, String serverUrl, String message) {
        ModelStatus status = available(modelName, serverUrl);
        status.message = message;
        return status;
    }
    
    // Factory methods para modelo no disponible
    public static ModelStatus unavailable(String modelName, String serverUrl) {
        ModelStatus status = new ModelStatus();
        status.available = false;
        status.modelName = modelName;
        status.serverUrl = serverUrl;
        status.message = "Modelo no disponible";
        return status;
    }
    
    public static ModelStatus unavailable(String modelName, String serverUrl, String message) {
        ModelStatus status = unavailable(modelName, serverUrl);
        status.message = message;
        return status;
    }
    
    // Builder-style methods
    public ModelStatus withVersion(String version) {
        this.version = version;
        return this;
    }
    
    public ModelStatus withModelSize(Long modelSize) {
        this.modelSize = modelSize;
        return this;
    }
    
    public ModelStatus withContextLength(Integer contextLength) {
        this.contextLength = contextLength;
        return this;
    }
    
    public ModelStatus withDetails(Map<String, Object> details) {
        this.details = details;
        return this;
    }
    
    public ModelStatus addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
    
    // Getters
    public boolean isAvailable() { 
        return available; 
    }
    
    public String getModelName() { 
        return modelName; 
    }
    
    public String getServerUrl() { 
        return serverUrl; 
    }
    
    public String getMessage() { 
        return message; 
    }
    
    public String getVersion() { 
        return version; 
    }
    
    public Long getModelSize() { 
        return modelSize; 
    }
    
    public Integer getContextLength() { 
        return contextLength; 
    }
    
    public Map<String, Object> getDetails() { 
        return details; 
    }
    
    @Override
    public String toString() {
        return "ModelStatus{" +
                "available=" + available +
                ", modelName='" + modelName + '\'' +
                ", serverUrl='" + serverUrl + '\'' +
                ", message='" + message + '\'' +
                ", version='" + version + '\'' +
                ", modelSize=" + modelSize +
                ", contextLength=" + contextLength +
                ", details=" + details +
                '}';
    }
}