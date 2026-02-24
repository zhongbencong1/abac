package com.abac.entity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * 用户实体。
 * 用于维护“谁”在做鉴权，subject_attrs 即该用户作为主体时的属性（如 role、department），鉴权时作为 subject 传入引擎。
 */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 登录名，唯一 */
    @NotBlank
    @Column(nullable = false, unique = true, length = 64)
    private String username;

    /** 显示名 */
    @Column(name = "display_name", length = 128)
    private String displayName;

    /**
     * 主体属性 JSON，如 {"role":"admin","department":"IT"}。
     * 鉴权时将该用户作为主体时，即使用此字段作为 subject。
     */
    @Column(name = "subject_attrs", columnDefinition = "TEXT")
    private String subjectAttrs;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getSubjectAttrs() { return subjectAttrs; }
    public void setSubjectAttrs(String subjectAttrs) { this.subjectAttrs = subjectAttrs; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
