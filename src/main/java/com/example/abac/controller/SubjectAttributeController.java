package com.example.abac.controller;

import com.example.abac.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectAttributeController {

    private final AttributeService attributeService;

    @GetMapping("/{subjectId}/attributes")
    public Map<String, String> getAttributes(@PathVariable String subjectId) {
        return attributeService.getSubjectAttributes(subjectId);
    }

    @PutMapping("/{subjectId}/attributes")
    public void setAttributes(@PathVariable String subjectId, @RequestBody Map<String, String> attributes) {
        attributeService.setSubjectAttributes(subjectId, attributes);
    }

    @PostMapping("/{subjectId}/attributes")
    public void addAttributes(@PathVariable String subjectId, @RequestBody Map<String, String> attributes) {
        attributeService.addSubjectAttributes(subjectId, attributes);
    }
}
