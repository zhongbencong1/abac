package com.example.abac.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckRequest {

    private String subjectId;
    private String resourceType;
    private String resourceId;
    private String action;
    private Map<String, Object> environment;
}
