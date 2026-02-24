package com.abac.controller;

import com.abac.dto.AuthzRequest;
import com.abac.dto.AuthzResponse;
import com.abac.service.AbacEngineService;
import com.abac.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

/**
 * 鉴权校验接口。
 * 根据请求中的主体、资源、操作、环境属性进行 ABAC 鉴权，并写入审计日志。
 */
@RestController
@RequestMapping("/api/authz")
public class AuthzController {

    private static final Logger log = LoggerFactory.getLogger(AuthzController.class);

    private final AbacEngineService abacEngine;
    private final AuditService auditService;

    public AuthzController(AbacEngineService abacEngine, AuditService auditService) {
        this.abacEngine = abacEngine;
        this.auditService = auditService;
    }

    /**
     * 执行鉴权校验。
     * 使用 ABAC 引擎按策略优先级匹配规则，返回是否允许及匹配的策略信息，并记录审计日志。
     *
     * @param request 鉴权请求，包含 subject（主体属性）、resource（资源属性）、action（操作）、environment（环境属性，可选）
     * @return 鉴权结果：allowed、reason、policyId、policyName
     */
    @PostMapping("/check")
    public AuthzResponse check(@RequestBody AuthzRequest request) {
        String action = request.getAction() != null ? request.getAction() : "";
        log.info("authz check action={}, subjectKeys={}, resourceKeys={}",
                action,
                request.getSubject() != null ? request.getSubject().keySet() : "null",
                request.getResource() != null ? request.getResource().keySet() : "null");

        AbacEngineService.AuthzResult result = abacEngine.authorize(
                request.getSubject(),
                request.getResource(),
                action,
                request.getEnvironment()
        );

        auditService.log(
                request.getSubject(),
                request.getResource(),
                request.getAction(),
                request.getEnvironment(),
                result.isAllowed(),
                result.getPolicyId(),
                result.getPolicyName(),
                result.getReason()
        );

        log.info("authz check result allowed={}, reason={}, policyName={}",
                result.isAllowed(), result.getReason(), result.getPolicyName());

        return new AuthzResponse(result.isAllowed(), result.getReason(), result.getPolicyId(), result.getPolicyName());
    }
}
