package com.tutict.eip.promptcompiler.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tutict.eip.promptcompiler.domain.DslDocument;
import com.tutict.eip.promptcompiler.exception.PromptCompilerException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JacksonYamlDslParser implements DslParser {

    private final ObjectMapper yamlObjectMapper;
    private final Validator validator;

    public JacksonYamlDslParser(@Qualifier("yamlObjectMapper") ObjectMapper yamlObjectMapper, Validator validator) {
        this.yamlObjectMapper = yamlObjectMapper;
        this.validator = validator;
    }

    @Override
    public DslDocument parse(String yaml) {
        try {
            DslDocument document = yamlObjectMapper.readValue(yaml, DslDocument.class);
            validate(document);
            return document;
        } catch (IOException ex) {
            throw new PromptCompilerException("Invalid YAML DSL: " + ex.getMessage(), ex);
        }
    }

    private void validate(DslDocument document) {
        Set<ConstraintViolation<DslDocument>> violations = validator.validate(document);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .collect(Collectors.joining("; "));
            throw new PromptCompilerException("Invalid DSL document: " + message);
        }
    }
}
