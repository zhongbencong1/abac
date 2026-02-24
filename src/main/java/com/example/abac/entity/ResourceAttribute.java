package com.example.abac.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resource_attribute", uniqueConstraints = @UniqueConstraint(columnNames = {"resource_type", "resource_id", "attr_key"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceAttribute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", nullable = false, length = 128)
    private String resourceId;

    @Column(name = "attr_key", nullable = false, length = 128)
    private String attrKey;

    @Column(name = "attr_value", nullable = false, length = 512)
    private String attrValue;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
