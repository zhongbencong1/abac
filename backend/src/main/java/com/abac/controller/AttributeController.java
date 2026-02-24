package com.abac.controller;

import com.abac.entity.AttributeEntity;
import com.abac.repository.AttributeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 属性管理接口。
 * 提供 ABAC 主体/资源/环境属性的增删改查，供策略规则引用（如 subject.role、resource.type）。
 */
@RestController
@RequestMapping("/api/attributes")
public class AttributeController {

    private static final Logger log = LoggerFactory.getLogger(AttributeController.class);

    private final AttributeRepository attributeRepository;

    public AttributeController(AttributeRepository attributeRepository) {
        this.attributeRepository = attributeRepository;
    }

    /**
     * 查询属性列表。
     * 按分类、名称排序返回全部已配置属性。
     *
     * @return 属性列表，按 category、name 升序
     */
    @GetMapping
    public List<AttributeEntity> list() {
        log.info("list attributes");
        List<AttributeEntity> list = attributeRepository.findAllByOrderByCategoryAscNameAsc();
        log.debug("list attributes result size={}", list != null ? list.size() : 0);
        return list;
    }

    /**
     * 根据 ID 查询单个属性。
     *
     * @param id 属性主键
     * @return 存在则返回 200 + 实体，否则 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttributeEntity> get(@PathVariable Long id) {
        log.info("get attribute id={}", id);
        return attributeRepository.findById(id)
                .map(entity -> {
                    log.debug("get attribute found id={}, name={}", id, entity.getName());
                    return ResponseEntity.ok(entity);
                })
                .orElseGet(() -> {
                    log.warn("get attribute not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 新增属性。
     * 需提供 name（唯一）、category（subject/resource/environment）、valueType（string/number/boolean）等。
     *
     * @param entity 属性实体，需通过校验
     * @return 持久化后的属性（含 id、createdAt 等）
     */
    @PostMapping
    public AttributeEntity create(@Valid @RequestBody AttributeEntity entity) {
        log.info("create attribute name={}, category={}, valueType={}", entity.getName(), entity.getCategory(), entity.getValueType());
        AttributeEntity saved = attributeRepository.save(entity);
        log.info("create attribute success id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    /**
     * 更新指定 ID 的属性。
     * 不修改 createdAt；若 id 不存在返回 404。
     *
     * @param id     属性主键
     * @param entity 新属性内容
     * @return 更新后的属性或 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<AttributeEntity> update(@PathVariable Long id, @Valid @RequestBody AttributeEntity entity) {
        log.info("update attribute id={}", id);
        return attributeRepository.findById(id)
                .map(existing -> {
                    entity.setId(id);
                    entity.setCreatedAt(existing.getCreatedAt());
                    AttributeEntity saved = attributeRepository.save(entity);
                    log.info("update attribute success id={}, name={}", id, saved.getName());
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    log.warn("update attribute not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 删除指定属性。
     *
     * @param id 属性主键
     * @return 成功 204，不存在 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("delete attribute id={}", id);
        if (!attributeRepository.existsById(id)) {
            log.warn("delete attribute not found id={}", id);
            return ResponseEntity.notFound().build();
        }
        attributeRepository.deleteById(id);
        log.info("delete attribute success id={}", id);
        return ResponseEntity.noContent().build();
    }
}
