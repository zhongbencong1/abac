package com.example.abac.controller;

import com.example.abac.dto.CheckRequest;
import com.example.abac.dto.CheckResponse;
import com.example.abac.service.PdpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/abac")
@RequiredArgsConstructor
public class AbacCheckController {

    private final PdpService pdpService;

    @PostMapping("/check")
    public CheckResponse check(@RequestBody CheckRequest request) {
        return pdpService.check(request);
    }
}
