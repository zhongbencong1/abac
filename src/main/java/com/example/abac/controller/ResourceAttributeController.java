package com.example.abac.controller;

import com.example.abac.service.AttributeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceAttributeController {

    private final AttributeService attributeService;

    @GetMapping("/{resourceType}/{resourceId}/attributes")
    public Map<String, String> getAttributes(@PathVariable String resourceType, @PathVariable String resourceId) {
        return attributeService.getResourceAttributes(resourceType, resourceId);
    }

    @PutMapping("/{resourceType}/{resourceId}/attributes")
    public void setAttributes(@PathVariable String resourceType, @PathVariable String resourceId,
                              @RequestBody Map<String, String> attributes) {
        attributeService.setResourceAttributes(resourceType, resourceId, attributes);
    }
}
