package com.example.abac.service;

import com.example.abac.entity.ResourceAttribute;
import com.example.abac.entity.SubjectAttribute;
import com.example.abac.repository.ResourceAttributeRepository;
import com.example.abac.repository.SubjectAttributeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttributeService {

    private final SubjectAttributeRepository subjectAttributeRepository;
    private final ResourceAttributeRepository resourceAttributeRepository;

    public Map<String, String> getSubjectAttributes(String subjectId) {
        return subjectAttributeRepository.findBySubjectId(subjectId).stream()
                .collect(Collectors.toMap(SubjectAttribute::getAttrKey, SubjectAttribute::getAttrValue, (a, b) -> b));
    }

    @Transactional
    public void setSubjectAttributes(String subjectId, Map<String, String> attributes) {
        subjectAttributeRepository.deleteBySubjectId(subjectId);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                subjectAttributeRepository.save(SubjectAttribute.builder()
                        .subjectId(subjectId)
                        .attrKey(e.getKey())
                        .attrValue(e.getValue())
                        .build());
            }
        }
    }

    @Transactional
    public void addSubjectAttributes(String subjectId, Map<String, String> attributes) {
        if (attributes == null) return;
        for (Map.Entry<String, String> e : attributes.entrySet()) {
            subjectAttributeRepository.findBySubjectId(subjectId).stream()
                    .filter(a -> a.getAttrKey().equals(e.getKey()))
                    .findFirst()
                    .ifPresent(subjectAttributeRepository::delete);
            subjectAttributeRepository.save(SubjectAttribute.builder()
                    .subjectId(subjectId)
                    .attrKey(e.getKey())
                    .attrValue(e.getValue())
                    .build());
        }
    }

    public Map<String, String> getResourceAttributes(String resourceType, String resourceId) {
        return resourceAttributeRepository.findByResourceTypeAndResourceId(resourceType, resourceId).stream()
                .collect(Collectors.toMap(ResourceAttribute::getAttrKey, ResourceAttribute::getAttrValue, (a, b) -> b));
    }

    @Transactional
    public void setResourceAttributes(String resourceType, String resourceId, Map<String, String> attributes) {
        resourceAttributeRepository.deleteByResourceTypeAndResourceId(resourceType, resourceId);
        if (attributes != null && !attributes.isEmpty()) {
            for (Map.Entry<String, String> e : attributes.entrySet()) {
                resourceAttributeRepository.save(ResourceAttribute.builder()
                        .resourceType(resourceType)
                        .resourceId(resourceId)
                        .attrKey(e.getKey())
                        .attrValue(e.getValue())
                        .build());
            }
        }
    }
}
