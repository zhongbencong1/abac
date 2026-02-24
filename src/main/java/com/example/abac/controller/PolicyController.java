package com.example.abac.controller;

import com.example.abac.dto.PolicyDto;
import com.example.abac.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;

    @GetMapping
    public Page<PolicyDto> list(@RequestParam(required = false) String name,
                                @RequestParam(required = false) Boolean enabled,
                                @PageableDefault(size = 20) Pageable pageable) {
        return policyService.list(name, enabled, pageable);
    }

    @GetMapping("/{id}")
    public PolicyDto get(@PathVariable Long id) {
        return policyService.get(id);
    }

    @PostMapping
    public PolicyDto create(@RequestBody PolicyDto dto) {
        return policyService.create(dto);
    }

    @PutMapping("/{id}")
    public PolicyDto update(@PathVariable Long id, @RequestBody PolicyDto dto) {
        return policyService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        policyService.delete(id);
    }

    @PatchMapping("/{id}/enabled")
    public PolicyDto setEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body != null ? body.get("enabled") : null;
        return policyService.setEnabled(id, Boolean.TRUE.equals(enabled));
    }
}
