package com.abac.service;

import com.abac.entity.PolicyEntity;
import com.abac.repository.PolicyRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ABAC 鉴权引擎服务。
 * <p>
 * 根据主体、资源、环境属性与策略规则求值，按策略优先级匹配，返回允许/拒绝及匹配的策略。
 * 规则表达式（rule_expression）为 JSON 数组，每项格式：{ "attribute": "subject.role", "op": "eq", "value": "admin" }，多条条件为逻辑与(AND)。
 * 支持操作符：eq、neq、in、not_in、gt、gte、lt、lte、exists、not_exists。
 * </p>
 */
@Service
public class AbacEngineService {

    private static final Logger log = LoggerFactory.getLogger(AbacEngineService.class);

    private final PolicyRepository policyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AbacEngineService(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * 从上下文中按「分类.属性名」取属性值。
     *
     * @param subject     主体属性
     * @param resource    资源属性
     * @param environment 环境属性
     * @param key         如 "subject.role"、"resource.type"
     * @return 属性值，不存在或 key 格式错误返回 null
     */
    @SuppressWarnings("unchecked")
    private Object getAttr(Map<String, Object> subject, Map<String, Object> resource, Map<String, Object> environment, String key) {
        String[] parts = key.split("\\.", 2);
        if (parts.length != 2) return null;
        String category = parts[0];
        String name = parts[1];
        Map<String, Object> map;
        if ("subject".equals(category)) {
            map = subject != null ? subject : Collections.<String, Object>emptyMap();
        } else if ("resource".equals(category)) {
            map = resource != null ? resource : Collections.<String, Object>emptyMap();
        } else if ("environment".equals(category)) {
            map = environment != null ? environment : Collections.<String, Object>emptyMap();
        } else {
            map = Collections.emptyMap();
        }
        return map.get(name);
    }

    /**
     * 对单条规则求值。
     *
     * @param rule        规则：attribute、op、value
     * @param subject     主体（含 action）
     * @param resource    资源
     * @param environment 环境
     * @return 是否满足该条规则
     */
    private boolean evaluateRule(Map<String, Object> rule, Map<String, Object> subject, Map<String, Object> resource, Map<String, Object> environment) {
        String attr = (String) rule.get("attribute");
        String op = (String) rule.get("op");
        Object expected = rule.get("value");
        Object actual = getAttr(subject, resource, environment, attr);

        String opVal = op != null ? op : "";
        if ("eq".equals(opVal)) {
            return (actual == null && expected == null) || (actual != null && actual.toString().equals(expected != null ? expected.toString() : null));
        } else if ("neq".equals(opVal)) {
            return (actual == null && expected != null) || (actual != null && !actual.toString().equals(expected != null ? expected.toString() : null));
        } else if ("in".equals(opVal)) {
            return expected instanceof List && ((List<?>) expected).stream().anyMatch(v -> v != null && v.toString().equals(actual != null ? actual.toString() : null));
        } else if ("not_in".equals(opVal)) {
            return !(expected instanceof List) || ((List<?>) expected).stream().noneMatch(v -> v != null && v.toString().equals(actual != null ? actual.toString() : null));
        } else if ("gt".equals(opVal)) {
            return toNum(actual) > toNum(expected);
        } else if ("gte".equals(opVal)) {
            return toNum(actual) >= toNum(expected);
        } else if ("lt".equals(opVal)) {
            return toNum(actual) < toNum(expected);
        } else if ("lte".equals(opVal)) {
            return toNum(actual) <= toNum(expected);
        } else if ("exists".equals(opVal)) {
            return actual != null && !actual.toString().isEmpty();
        } else if ("not_exists".equals(opVal)) {
            return actual == null || actual.toString().isEmpty();
        }
        return false;
    }

    private double toNum(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        try {
            return Double.parseDouble(o.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 执行鉴权。
     * 按优先级顺序匹配已启用策略，第一条全部规则满足的策略决定结果（allow/deny）；无匹配时默认拒绝。
     *
     * @param subject     主体属性（可为空，内部会转为空 Map）
     * @param resource    资源属性（可为空）
     * @param action      操作名，会并入 subject 参与规则求值
     * @param environment 环境属性（可为空）
     * @return 鉴权结果：是否允许、原因、匹配的策略 ID 与名称
     */
    public AuthzResult authorize(Map<String, Object> subject, Map<String, Object> resource, String action, Map<String, Object> environment) {
        if (subject == null) subject = Collections.emptyMap();
        if (resource == null) resource = Collections.emptyMap();
        if (environment == null) environment = Collections.emptyMap();
        @SuppressWarnings("unchecked")
        Map<String, Object> subjectWithAction = subject.containsKey("action") ? subject : new HashMap<>(subject);
        subjectWithAction.put("action", action);

        List<PolicyEntity> policies = policyRepository.findByEnabledTrueOrderByPriorityAscIdAsc();
        log.debug("authorize evaluating {} enabled policies, action={}", policies.size(), action);

        for (PolicyEntity policy : policies) {
            try {
                List<Map<String, Object>> expr = objectMapper.readValue(policy.getRuleExpression(), new TypeReference<List<Map<String, Object>>>() {});
                if (expr == null) continue;
                boolean allMatch = true;
                for (Map<String, Object> rule : expr) {
                    if (!evaluateRule(rule, subjectWithAction, resource, environment)) {
                        allMatch = false;
                        break;
                    }
                }
                if (allMatch) {
                    boolean allow = policy.getEffect() == PolicyEntity.PolicyEffect.allow;
                    log.debug("authorize matched policy id={}, name={}, effect={}", policy.getId(), policy.getName(), policy.getEffect());
                    return new AuthzResult(allow, "Matched policy: " + policy.getName(), policy.getId(), policy.getName());
                }
            } catch (Exception e) {
                log.warn("authorize skip invalid policy id={}, name={}, error={}", policy.getId(), policy.getName(), e.getMessage());
            }
        }
        log.debug("authorize no policy matched, default deny");
        return new AuthzResult(false, "No matching policy (default deny)", null, null);
    }

    /**
     * 鉴权结果：是否允许、原因说明、匹配的策略 ID 与名称。
     */
    public static class AuthzResult {
        private final boolean allowed;
        private final String reason;
        private final Long policyId;
        private final String policyName;

        public AuthzResult(boolean allowed, String reason, Long policyId, String policyName) {
            this.allowed = allowed;
            this.reason = reason;
            this.policyId = policyId;
            this.policyName = policyName;
        }

        public boolean isAllowed() { return allowed; }
        public String getReason() { return reason; }
        public Long getPolicyId() { return policyId; }
        public String getPolicyName() { return policyName; }
    }
}
