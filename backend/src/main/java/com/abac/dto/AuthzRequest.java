package com.abac.dto;

import java.util.Map;

public class AuthzRequest {

    private Map<String, Object> subject;
    private Map<String, Object> resource;
    private String action;
    private Map<String, Object> environment;

    public Map<String, Object> getSubject() { return subject; }
    public void setSubject(Map<String, Object> subject) { this.subject = subject; }
    public Map<String, Object> getResource() { return resource; }
    public void setResource(Map<String, Object> resource) { this.resource = resource; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Map<String, Object> getEnvironment() { return environment; }
    public void setEnvironment(Map<String, Object> environment) { this.environment = environment; }
}
