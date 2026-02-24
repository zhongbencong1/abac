package com.abac.service;

import com.abac.entity.AuditLogEntity;
import com.abac.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 审计日志服务。
 * 负责写入鉴权审计记录，以及分页查询审计日志。
 */
@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * 写入一条鉴权审计记录。
     * 将 subject、resource、environment 序列化为 JSON 存储。
     *
     * @param subject     主体属性
     * @param resource    资源属性
     * @param action      操作
     * @param environment 环境属性
     * @param allowed     鉴权结果是否允许
     * @param policyId    匹配的策略 ID，无匹配可为 null
     * @param policyName  匹配的策略名称，无匹配可为 null
     * @param reason      结果原因说明
     * @return 持久化后的审计实体
     */
    public AuditLogEntity log(Map<String, Object> subject, Map<String, Object> resource, String action,
                              Map<String, Object> environment, boolean allowed, Long policyId, String policyName, String reason) {
        log.debug("audit log action={}, allowed={}, policyName={}", action, allowed, policyName);
        AuditLogEntity logEntity = new AuditLogEntity();
        logEntity.setSubjectAttrs(toJson(subject));
        logEntity.setResourceAttrs(toJson(resource));
        logEntity.setAction(action);
        logEntity.setEnvironmentAttrs(toJson(environment));
        logEntity.setResult(allowed ? AuditLogEntity.AuditResult.allow : AuditLogEntity.AuditResult.deny);
        logEntity.setPolicyId(policyId);
        logEntity.setPolicyName(policyName);
        logEntity.setReason(reason);
        AuditLogEntity saved = auditLogRepository.save(logEntity);
        log.debug("audit log saved id={}", saved.getId());
        return saved;
    }

    private String toJson(Object o) {
        if (o == null) return null;
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.warn("audit toJson error: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 分页查询审计日志，按创建时间倒序。
     *
     * @param page 页码，从 0 开始
     * @param size 每页条数
     * @return 分页结果
     */
    public Page<AuditLogEntity> list(int page, int size) {
        log.debug("audit list page={}, size={}", page, size);
        Pageable p = PageRequest.of(page, size);
        return auditLogRepository.findAllByOrderByCreatedAtDesc(p);
    }
}
