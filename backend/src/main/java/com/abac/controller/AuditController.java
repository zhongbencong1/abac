package com.abac.controller;

import com.abac.entity.AuditLogEntity;
import com.abac.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

/**
 * 审计日志接口。
 * 提供鉴权记录的分页查询，便于审计与排查。
 */
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private static final Logger log = LoggerFactory.getLogger(AuditController.class);

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    /**
     * 分页查询审计日志。
     * 按创建时间倒序，最新记录在前。
     *
     * @param page 页码，从 0 开始，默认 0
     * @param size 每页条数，默认 20
     * @return 分页结果，包含 content、totalElements、totalPages 等
     */
    @GetMapping("/logs")
    public Page<AuditLogEntity> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("audit logs page={}, size={}", page, size);
        Page<AuditLogEntity> result = auditService.list(page, size);
        log.debug("audit logs result totalElements={}, totalPages={}", result.getTotalElements(), result.getTotalPages());
        return result;
    }
}
