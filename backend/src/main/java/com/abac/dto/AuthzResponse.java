package com.abac.dto;

public class AuthzResponse {

    private boolean allowed;
    private String reason;
    private Long policyId;
    private String policyName;

    public AuthzResponse(boolean allowed, String reason, Long policyId, String policyName) {
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
