package com.tutict.eip.harness.controller;

import com.tutict.eip.harness.domain.CompileRequest;
import com.tutict.eip.harness.domain.CompileResponse;
import com.tutict.eip.harness.service.CompileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CompileController {

    private final CompileService compileService;

    public CompileController(CompileService compileService) {
        this.compileService = compileService;
    }

    @PostMapping("/compile")
    public CompileResponse compile(@Valid @RequestBody CompileRequest request) {
        return compileService.compile(request.getRequirement());
    }
}
