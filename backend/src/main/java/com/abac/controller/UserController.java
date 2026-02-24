package com.abac.controller;

import com.abac.entity.UserEntity;
import com.abac.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 用户管理接口。
 * 维护用户及其主体属性（subject_attrs），鉴权时可将用户解析为主体属性参与 ABAC 校验。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 查询用户列表，按用户名升序。
     *
     * @return 用户列表
     */
    @GetMapping
    public List<UserEntity> list() {
        log.info("list users");
        List<UserEntity> list = userRepository.findAllByOrderByUsernameAsc();
        log.debug("list users result size={}", list != null ? list.size() : 0);
        return list;
    }

    /**
     * 根据 ID 查询单个用户。
     *
     * @param id 用户主键
     * @return 存在则 200 + 实体，否则 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> get(@PathVariable Long id) {
        log.info("get user id={}", id);
        return userRepository.findById(id)
                .map(entity -> {
                    log.debug("get user found id={}, username={}", id, entity.getUsername());
                    return ResponseEntity.ok(entity);
                })
                .orElseGet(() -> {
                    log.warn("get user not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 获取指定用户的主体属性（用于鉴权时的 subject）。
     * 将用户的 subject_attrs JSON 解析为 Map 返回；若为空或解析失败返回空对象。
     *
     * @param id 用户主键
     * @return 主体属性 Map，或 404
     */
    @GetMapping("/{id}/subject-attrs")
    public ResponseEntity<Map<String, Object>> getSubjectAttrs(@PathVariable Long id) {
        log.info("get user subject-attrs id={}", id);
        return userRepository.findById(id)
                .map(user -> {
                    Map<String, Object> attrs = parseSubjectAttrs(user.getSubjectAttrs());
                    log.debug("get user subject-attrs id={}, keys={}", id, attrs != null ? attrs.keySet() : "{}");
                    return ResponseEntity.ok(attrs);
                })
                .orElseGet(() -> {
                    log.warn("get user subject-attrs not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    private Map<String, Object> parseSubjectAttrs(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.<String, Object>emptyMap();
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("parse subject_attrs failed: {}", e.getMessage());
            return Collections.<String, Object>emptyMap();
        }
    }

    /**
     * 新增用户。
     *
     * @param entity 用户实体，username 必填且唯一
     * @return 持久化后的用户
     */
    @PostMapping
    public UserEntity create(@Valid @RequestBody UserEntity entity) {
        log.info("create user username={}, displayName={}", entity.getUsername(), entity.getDisplayName());
        UserEntity saved = userRepository.save(entity);
        log.info("create user success id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * 更新指定用户。
     *
     * @param id     用户主键
     * @param entity 新内容
     * @return 更新后的用户或 404
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> update(@PathVariable Long id, @Valid @RequestBody UserEntity entity) {
        log.info("update user id={}", id);
        return userRepository.findById(id)
                .map(existing -> {
                    entity.setId(id);
                    entity.setCreatedAt(existing.getCreatedAt());
                    UserEntity saved = userRepository.save(entity);
                    log.info("update user success id={}, username={}", id, saved.getUsername());
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> {
                    log.warn("update user not found id={}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 删除指定用户。
     *
     * @param id 用户主键
     * @return 成功 204，不存在 404
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("delete user id={}", id);
        if (!userRepository.existsById(id)) {
            log.warn("delete user not found id={}", id);
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        log.info("delete user success id={}", id);
        return ResponseEntity.noContent().build();
    }
}
