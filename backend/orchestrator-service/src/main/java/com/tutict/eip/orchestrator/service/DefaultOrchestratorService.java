package com.tutict.eip.orchestrator.service;

import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.service.AutoRepairGenerationService;
import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.service.CompileService;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultOrchestratorService implements OrchestratorService {

    private final CompileService compileService;
    private final AutoRepairGenerationService autoRepairGenerationService;

    public DefaultOrchestratorService(
            CompileService compileService,
            AutoRepairGenerationService autoRepairGenerationService
    ) {
        this.compileService = compileService;
        this.autoRepairGenerationService = autoRepairGenerationService;
    }

    @Override
    public OrchestratorRunResponse run(OrchestratorRunRequest request) {
        CompileResponse compiled = compileService.compile(request.getRequirement());
        AutoRepairGenerationResponse generation = autoRepairGenerationService.generateAndRepair(
                buildGenerationRequest(request, compiled.getPrompt())
        );
        return new OrchestratorRunResponse(
                UUID.randomUUID().toString(),
                compiled.getDsl(),
                compiled.getPrompt(),
                generation,
                Instant.now()
        );
    }

    private AutoRepairGenerationRequest buildGenerationRequest(OrchestratorRunRequest request, String prompt) {
        AutoRepairGenerationRequest generationRequest = new AutoRepairGenerationRequest();
        generationRequest.setModel(request.getModel());
        generationRequest.setPrompt(prompt);
        generationRequest.setTargetDirectory(request.getTargetDirectory());
        generationRequest.setVerifyCommands(resolveVerifyCommands(request));
        generationRequest.setMaxRepairRounds(request.getMaxRepairRounds());
        generationRequest.setOptions(request.getOptions());
        return generationRequest;
    }

    private List<List<String>> resolveVerifyCommands(OrchestratorRunRequest request) {
        if (request.getVerifyCommands() == null || request.getVerifyCommands().isEmpty()) {
            return List.of(List.of("mvn", "test"));
        }
        return request.getVerifyCommands();
    }
}
