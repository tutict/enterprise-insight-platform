package com.tutict.eip.harnesscompiler.service;

import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.domain.DslModel;
import com.tutict.eip.harnesscompiler.domain.graph.GraphDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompileService {

    private static final Logger log = LoggerFactory.getLogger(CompileService.class);

    private final DslParser dslParser;
    private final PromptCompiler promptCompiler;
    private final GraphToDslCompiler graphToDslCompiler;

    public CompileService(DslParser dslParser, PromptCompiler promptCompiler) {
        this(dslParser, promptCompiler, new GraphToDslCompiler());
    }

    @Autowired
    public CompileService(DslParser dslParser, PromptCompiler promptCompiler, GraphToDslCompiler graphToDslCompiler) {
        this.dslParser = dslParser;
        this.promptCompiler = promptCompiler;
        this.graphToDslCompiler = graphToDslCompiler;
    }

    public CompileResponse compile(String requirement) {
        log.info("Compile request received requirementChars={}", requirement == null ? 0 : requirement.length());
        DslModel dslModel = dslParser.parse(requirement);
        String prompt = promptCompiler.compile(dslModel);
        return new CompileResponse(dslModel, prompt);
    }

    public CompileResponse compileFromGraph(GraphDefinition graph) {
        log.info("Compile from graph request received graphId={} nodeCount={}",
                graph == null ? null : graph.getId(),
                graph == null || graph.getNodes() == null ? 0 : graph.getNodes().size());
        DslModel dslModel = graphToDslCompiler.compile(graph);
        String prompt = promptCompiler.compile(dslModel);
        return new CompileResponse(dslModel, prompt);
    }
}
