package com.example.abac.service;

import com.example.abac.dto.CheckRequest;
import com.example.abac.dto.CheckResponse;
import com.example.abac.entity.Policy;
import com.example.abac.repository.PolicyRepository;
import com.example.abac.entity.ResourceAttribute;
import com.example.abac.repository.ResourceAttributeRepository;
import com.example.abac.repository.SubjectAttributeRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Policy Decision Point: 根据请求上下文与策略条件做鉴权决策。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PdpService {

    private final SubjectAttributeRepository subjectAttributeRepository;
    private final ResourceAttributeRepository resourceAttributeRepository;
    private final PolicyRepository policyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CheckResponse check(CheckRequest request) {
        Map<String, String> subjectAttrs = loadSubjectAttributes(request.getSubjectId());
        Map<String, String> resourceAttrs = loadResourceAttributes(request.getResourceType(), request.getResourceId());
        String action = request.getAction() != null ? request.getAction() : "";

        List<Policy> policies = policyRepository.findByEnabledTrueOrderByPriorityDesc();
        for (Policy policy : policies) {
            if (matchCondition(policy.getConditionJson(), subjectAttrs, resourceAttrs, action, request.getEnvironment())) {
                boolean allow = "ALLOW".equalsIgnoreCase(policy.getEffect());
                return CheckResponse.builder()
                        .allowed(allow)
                        .policyId(policy.getId())
                        .message(allow ? null : "Denied by policy: " + policy.getName())
                        .build();
            }
        }
        return CheckResponse.builder().allowed(false).policyId(null).message("No matching policy").build();
    }

    private Map<String, String> loadSubjectAttributes(String subjectId) {
        if (subjectId == null) return Collections.emptyMap();
        return subjectAttributeRepository.findBySubjectId(subjectId).stream()
                .collect(Collectors.toMap(a -> a.getAttrKey(), a -> a.getAttrValue(), (a, b) -> b));
    }

    private Map<String, String> loadResourceAttributes(String resourceType, String resourceId) {
        if (resourceType == null || resourceId == null) return Collections.emptyMap();
        return resourceAttributeRepository.findByResourceTypeAndResourceId(resourceType, resourceId).stream()
                .collect(Collectors.toMap(ResourceAttribute::getAttrKey, ResourceAttribute::getAttrValue, (a, b) -> b));
    }

    private boolean matchCondition(String conditionJson, Map<String, String> subjectAttrs,
                                   Map<String, String> resourceAttrs, String action,
                                   Map<String, Object> environment) {
        try {
            JsonNode root = objectMapper.readTree(conditionJson);

            JsonNode subjectCond = root.path("subject");
            if (!subjectCond.isMissingNode() && !matchAttrCondition(subjectCond, subjectAttrs)) return false;

            JsonNode resourceCond = root.path("resource");
            if (!resourceCond.isMissingNode() && !matchAttrCondition(resourceCond, resourceAttrs)) return false;

            JsonNode actionCond = root.path("action");
            if (!actionCond.isMissingNode() && !actionCond.isNull()) {
                if (actionCond.isArray()) {
                    boolean found = false;
                    for (JsonNode v : actionCond) {
                        if (action.equals(v.asText())) { found = true; break; }
                    }
                    if (!found) return false;
                } else if (actionCond.isTextual()) {
                    if (!action.equals(actionCond.asText())) return false;
                }
            }

            JsonNode envCond = root.path("environment");
            if (!envCond.isMissingNode() && environment != null && !matchEnvCondition(envCond, environment)) return false;

            return true;
        } catch (Exception e) {
            log.warn("Policy condition parse error: {}", e.getMessage());
            return false;
        }
    }

    private boolean matchAttrCondition(JsonNode cond, Map<String, String> attrs) {
        Iterator<String> it = cond.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            JsonNode required = cond.get(key);
            String actual = attrs.get(key);
            if (required.isArray()) {
                boolean ok = false;
                for (JsonNode v : required) {
                    if (v.asText().equals(actual)) { ok = true; break; }
                }
                if (!ok) return false;
            } else {
                if (!required.asText().equals(actual)) return false;
            }
        }
        return true;
    }

    private boolean matchEnvCondition(JsonNode cond, Map<String, Object> env) {
        JsonNode ipPrefix = cond.path("ipPrefix");
        if (!ipPrefix.isMissingNode() && ipPrefix.isTextual()) {
            String prefix = ipPrefix.asText().trim();
            if (!prefix.isEmpty()) {
                Object ip = env.get("clientIp");
                if (ip == null) return false;
                if (!String.valueOf(ip).startsWith(prefix)) return false;
            }
        }
        return true;
    }
}
