package com.tutict.eip.harness.service;

import com.tutict.eip.harness.agent.AgentAdapter;
import com.tutict.eip.harness.agent.AgentExecutionResult;
import com.tutict.eip.harness.compiler.HarnessCompiler;
import com.tutict.eip.harness.domain.CompiledHarnessPrompt;
import com.tutict.eip.harness.domain.DslDocument;
import com.tutict.eip.harness.domain.HarnessRunRequest;
import com.tutict.eip.harness.domain.HarnessRunResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultHarnessService implements HarnessService {

    private final HarnessCompiler harnessCompiler;
    private final AgentAdapter agentAdapter;

    public DefaultHarnessService(HarnessCompiler harnessCompiler, AgentAdapter agentAdapter) {
        this.harnessCompiler = harnessCompiler;
        this.agentAdapter = agentAdapter;
    }

    @Override
    public HarnessRunResponse run(HarnessRunRequest request) {
        DslDocument dslDocument = new DslDocument(request.getProjectType(), request.getModules(), request.getConstraints());
        CompiledHarnessPrompt compiledPrompt = harnessCompiler.compile(dslDocument);
        AgentExecutionResult executionResult = agentAdapter.execute(compiledPrompt, request.getTargetPath());
        return new HarnessRunResponse(
                UUID.randomUUID().toString(),
                compiledPrompt.getPrompt(),
                executionResult.getOutputPath(),
                executionResult.getStatus(),
                Instant.now()
        );
    }
}
