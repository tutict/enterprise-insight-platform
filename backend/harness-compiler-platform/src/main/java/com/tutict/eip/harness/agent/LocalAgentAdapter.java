package com.tutict.eip.harness.agent;

import com.tutict.eip.harness.domain.CompiledHarnessPrompt;
import org.springframework.stereotype.Component;

@Component
public class LocalAgentAdapter implements AgentAdapter {

    @Override
    public AgentExecutionResult execute(CompiledHarnessPrompt prompt, String targetPath) {
        return new AgentExecutionResult(targetPath, "CREATED");
    }
}
