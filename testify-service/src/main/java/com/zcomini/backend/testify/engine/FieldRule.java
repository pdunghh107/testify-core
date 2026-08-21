package com.zcomini.backend.testify.engine;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FieldRule {
    private String type; // string, integer, boolean, object, array
    private boolean required = true; // Default to true if not specified
    private Boolean unique;
    
    // String rules
    private Integer minLength;
    private Integer maxLength;
    private String pattern;
    private String format; // email, uuid, url, phone
    
    // Number rules
    private Integer min;
    private Integer max;
    
    // Enum
    private List<String> enumValues;
}
