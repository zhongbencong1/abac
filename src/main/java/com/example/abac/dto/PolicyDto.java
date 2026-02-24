package com.example.abac.dto;

import com.example.abac.entity.Policy;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class PolicyDto {

    private Long id;
    private String name;
    private String effect;
    private int priority;
    private String conditionJson;  // 存 JSON 字符串，便于前端展示/编辑
    private boolean enabled;

    public static PolicyDto from(Policy p) {
        PolicyDto dto = new PolicyDto();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setEffect(p.getEffect());
        dto.setPriority(p.getPriority());
        dto.setConditionJson(p.getConditionJson());
        dto.setEnabled(p.isEnabled());
        return dto;
    }

    public Policy toEntity() {
        return Policy.builder()
                .id(id)
                .name(name)
                .effect(effect)
                .priority(priority)
                .conditionJson(conditionJson)
                .enabled(enabled)
                .build();
    }
}
