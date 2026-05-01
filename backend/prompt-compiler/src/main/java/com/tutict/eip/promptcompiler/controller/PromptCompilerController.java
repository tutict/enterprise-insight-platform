package com.tutict.eip.promptcompiler.controller;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.promptcompiler.domain.PromptCompileRequest;
import com.tutict.eip.promptcompiler.domain.PromptCompileResponse;
import com.tutict.eip.promptcompiler.service.PromptCompilerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prompt-compiler")
public class PromptCompilerController {

    private final PromptCompilerService promptCompilerService;

    public PromptCompilerController(PromptCompilerService promptCompilerService) {
        this.promptCompilerService = promptCompilerService;
    }

    @PostMapping("/compile")
    public ApiResponse<PromptCompileResponse> compile(@Valid @RequestBody PromptCompileRequest request) {
        return ApiResponse.ok(promptCompilerService.compile(request));
    }
}
