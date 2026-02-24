package com.abac.controller;

import com.abac.entity.PolicyEntity;
import com.abac.repository.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 策略管理接口。
 * 提供 ABAC 策略的增删改查，策略包含规则表达式（ruleExpression）与效果（allow/deny）、优先级等。
 */
@RestController
@RequestMapping("/api/policies")
public class PolicyController {

    private static final Logger log = LoggerFactory.getLogger(PolicyController.class);

    private final PolicyRepository policyRepository;

    public PolicyController(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    /**
     * 查询策略列表。
     * 返回所有策略（含启用与禁用），不排序。
     *
     * @return 策略列表
     */
    @GetMapping
    public List<PolicyEntity> list() {
        log.info("list policies");
        List<PolicyEntity> list = policyRepository.findAll();
        log.debug("list policies result size={}", list != null ? list.size() : 0);
        return list;
    }

    /**
     * 根据 ID 查询单个策略。
     *
     * @param id 策略主键
     * @return 存在则 200 + 实体，否则 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<PolicyEntity> get(@PathVariable Long id) {
        log.info("get policy id={}", id);
        return policyRepository.findById(id)
                .map(entity -> {
                    log.debug("get policy found id={}, name={}", id, entity.getName());
                    return ResponseEntity.ok(entity);
                })
                .orElseGet(() -> {
                    log.warn("get policy not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 新增策略。
     * 需提供 name、effect（allow/deny）、ruleExpression（JSON 数组）、priority、enabled 等。
     *
     * @param entity 策略实体，需通过校验
     * @return 持久化后的策略（含 id、createdAt 等）
     */
    @PostMapping
    public PolicyEntity create(@Valid @RequestBody PolicyEntity entity) {
        log.info("create policy name={}, effect={}, priority={}", entity.getName(), entity.getEffect(), entity.getPriority());
        PolicyEntity saved = policyRepository.save(entity);
        log.info("create policy success id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * 更新指定 ID 的策略。
     * 不修改 createdAt；若 id 不存在返回 404。
     *
     * @param id     策略主键
     * @param entity 新策略内容
     * @return 更新后的策略或 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<PolicyEntity> update(@PathVariable Long id, @Valid @RequestBody PolicyEntity entity) {
        log.info("update policy id={}", id);
        return policyRepository.findById(id)
                .map(existing -> {
                    entity.setId(id);
                    entity.setCreatedAt(existing.getCreatedAt());
                    PolicyEntity saved = policyRepository.save(entity);
                    log.info("update policy success id={}, name={}", id, saved.getName());
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    log.warn("update policy not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 删除指定策略。
     *
     * @param id 策略主键
     * @return 成功 204，不存在 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("delete policy id={}", id);
        if (!policyRepository.existsById(id)) {
            log.warn("delete policy not found id={}", id);
            return ResponseEntity.notFound().build();
        }
        policyRepository.deleteById(id);
        log.info("delete policy success id={}", id);
        return ResponseEntity.noContent().build();
    }
}
