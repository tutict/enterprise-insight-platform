package com.tutict.eip.harness.controller;

import com.tutict.eip.harness.domain.GenerateRequest;
import com.tutict.eip.harness.domain.GenerateResponse;
import com.tutict.eip.harness.service.CodeGenerationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GenerateController {

    private final CodeGenerationService codeGenerationService;

    public GenerateController(CodeGenerationService codeGenerationService) {
        this.codeGenerationService = codeGenerationService;
    }

    @PostMapping("/generate")
    public GenerateResponse generate(@Valid @RequestBody GenerateRequest request) {
        return new GenerateResponse(codeGenerationService.generateCode(request.getPrompt()));
    }
}
