package com.tutict.eip.promptcompiler.service;

public interface PromptTemplateRegistry {

    PromptTemplate resolve(String templateName);
}
