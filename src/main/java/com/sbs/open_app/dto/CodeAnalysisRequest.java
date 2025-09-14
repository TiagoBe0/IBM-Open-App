package com.sbs.open_app.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * DTO específico para solicitudes de análisis de código
 */
public class CodeAnalysisRequest {
    
    @NotBlank(message = "El código no puede estar vacío")
    private String code;
    
    private String language = "java";
    private String focus;
    private String analysisType = "quality"; // quality, security, performance, etc.
    private Map<String, Object> options;
    
    // Constructores
    public CodeAnalysisRequest() {}
    
    public CodeAnalysisRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }
    
    // Getters y Setters
    public String getCode() { 
        return code; 
    }
    
    public void setCode(String code) { 
        this.code = code; 
    }
    
    public String getLanguage() { 
        return language; 
    }
    
    public void setLanguage(String language) { 
        this.language = language; 
    }
    
    public String getFocus() { 
        return focus; 
    }
    
    public void setFocus(String focus) { 
        this.focus = focus; 
    }
    
    public String getAnalysisType() { 
        return analysisType; 
    }
    
    public void setAnalysisType(String analysisType) { 
        this.analysisType = analysisType; 
    }
    
    public Map<String, Object> getOptions() { 
        return options; 
    }
    
    public void setOptions(Map<String, Object> options) { 
        this.options = options; 
    }
    
    @Override
    public String toString() {
        return "CodeAnalysisRequest{" +
                "code='" + code + '\'' +
                ", language='" + language + '\'' +
                ", focus='" + focus + '\'' +
                ", analysisType='" + analysisType + '\'' +
                ", options=" + options +
                '}';
    }
}