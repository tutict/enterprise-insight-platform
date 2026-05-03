package com.tutict.eip.harnesscompiler.controller;

import com.tutict.eip.harnesscompiler.domain.CompileRequest;
import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.service.CompileService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/compiler")
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
