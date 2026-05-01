package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AgentExecutionResponse;

import java.util.function.Consumer;

public interface AgentExecutionService {

    AgentExecutionResponse execute(AgentExecutionRequest request);

    void stream(
            AgentExecutionRequest request,
            Consumer<String> tokenConsumer,
            Consumer<AgentExecutionResponse> completionConsumer
    );
}
