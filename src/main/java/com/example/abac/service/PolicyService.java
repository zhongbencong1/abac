package com.example.abac.service;

import com.example.abac.dto.PolicyDto;
import com.example.abac.entity.Policy;
import com.example.abac.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;

    public Page<PolicyDto> list(String name, Boolean enabled, Pageable pageable) {
        return policyRepository.filter(name, enabled, pageable).map(PolicyDto::from);
    }

    public PolicyDto get(Long id) {
        return policyRepository.findById(id).map(PolicyDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found: " + id));
    }

    @Transactional
    public PolicyDto create(PolicyDto dto) {
        Policy p = dto.toEntity();
        p.setId(null);
        p = policyRepository.save(p);
        return PolicyDto.from(p);
    }

    @Transactional
    public PolicyDto update(Long id, PolicyDto dto) {
        Policy p = policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found: " + id));
        p.setName(dto.getName());
        p.setEffect(dto.getEffect());
        p.setPriority(dto.getPriority());
        p.setConditionJson(dto.getConditionJson());
        p.setEnabled(dto.isEnabled());
        p = policyRepository.save(p);
        return PolicyDto.from(p);
    }

    @Transactional
    public void delete(Long id) {
        policyRepository.deleteById(id);
    }

    @Transactional
    public PolicyDto setEnabled(Long id, boolean enabled) {
        Policy p = policyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Policy not found: " + id));
        p.setEnabled(enabled);
        p = policyRepository.save(p);
        return PolicyDto.from(p);
    }
}
