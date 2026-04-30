package com.tutict.eip.harness.agent;

import com.tutict.eip.harness.domain.CompiledHarnessPrompt;

public interface AgentAdapter {

    AgentExecutionResult execute(CompiledHarnessPrompt prompt, String targetPath);
}
