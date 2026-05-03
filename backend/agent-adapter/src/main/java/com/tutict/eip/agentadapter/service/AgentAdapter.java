package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;

import java.util.function.Consumer;

public interface AgentAdapter {

    String provider();

    OllamaGenerationResult generate(AgentExecutionRequest request, Consumer<String> tokenConsumer);
}
