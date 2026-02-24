package com.abac.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Table(name = "audit_logs")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subject_attrs", columnDefinition = "TEXT")
    private String subjectAttrs;

    @Column(name = "resource_attrs", columnDefinition = "TEXT")
    private String resourceAttrs;

    private String action;

    @Column(name = "environment_attrs", columnDefinition = "TEXT")
    private String environmentAttrs;

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AuditResult result;

    @Column(name = "policy_id")
    private Long policyId;

    @Column(name = "policy_name")
    private String policyName;

    private String reason;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public enum AuditResult { allow, deny }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSubjectAttrs() { return subjectAttrs; }
    public void setSubjectAttrs(String subjectAttrs) { this.subjectAttrs = subjectAttrs; }
    public String getResourceAttrs() { return resourceAttrs; }
    public void setResourceAttrs(String resourceAttrs) { this.resourceAttrs = resourceAttrs; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEnvironmentAttrs() { return environmentAttrs; }
    public void setEnvironmentAttrs(String environmentAttrs) { this.environmentAttrs = environmentAttrs; }
    public AuditResult getResult() { return result; }
    public void setResult(AuditResult result) { this.result = result; }
    public Long getPolicyId() { return policyId; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public String getPolicyName() { return policyName; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
