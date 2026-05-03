package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AgentExecutionResponse;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.domain.WrittenFile;
import com.tutict.eip.agentadapter.storage.CodeFileWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class DefaultAgentExecutionService implements AgentExecutionService {

    private static final Logger log = LoggerFactory.getLogger(DefaultAgentExecutionService.class);

    private final AgentAdapter agentAdapter;
    private final CodeFileWriter codeFileWriter;

    public DefaultAgentExecutionService(AgentAdapter agentAdapter, CodeFileWriter codeFileWriter) {
        this.agentAdapter = agentAdapter;
        this.codeFileWriter = codeFileWriter;
    }

    @Override
    public AgentExecutionResponse execute(AgentExecutionRequest request) {
        log.info("Executing agent request provider={} model={} targetPath={}",
                agentAdapter.provider(), request.getModel(), request.getTargetPath());
        OllamaGenerationResult result = agentAdapter.generate(request, ignored -> {
        });
        return writeAndBuildResponse(request, result);
    }

    @Override
    public void stream(
            AgentExecutionRequest request,
            Consumer<String> tokenConsumer,
            Consumer<AgentExecutionResponse> completionConsumer
    ) {
        log.info("Streaming agent request provider={} model={} targetPath={}",
                agentAdapter.provider(), request.getModel(), request.getTargetPath());
        OllamaGenerationResult result = agentAdapter.generate(request, tokenConsumer);
        completionConsumer.accept(writeAndBuildResponse(request, result));
    }

    private AgentExecutionResponse writeAndBuildResponse(AgentExecutionRequest request, OllamaGenerationResult result) {
        WrittenFile writtenFile = codeFileWriter.write(request.getTargetPath(), result.getContent());
        log.info("Agent execution completed provider={} model={} targetPath={} bytesWritten={} attempts={}",
                agentAdapter.provider(), result.getModel(), writtenFile.getTargetPath(),
                writtenFile.getBytesWritten(), result.getAttemptCount());
        return new AgentExecutionResponse(
                agentAdapter.provider(),
                result.getModel(),
                writtenFile.getTargetPath(),
                writtenFile.getAbsolutePath(),
                result.getContent(),
                result.getAttemptCount(),
                result.getDurationMillis(),
                writtenFile.getBytesWritten()
        );
    }
}
