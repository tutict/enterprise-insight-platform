package com.tutict.eip.orchestrator.runtime;

import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.service.AutoRepairGenerationService;
import com.tutict.eip.harnesscompiler.domain.CompileResponse;
import com.tutict.eip.harnesscompiler.service.CompileService;
import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RunExecutionEngine {

    private final CompileService compileService;
    private final AutoRepairGenerationService autoRepairGenerationService;
    private final RunEventStreamService streamService;

    public RunExecutionEngine(
            CompileService compileService,
            AutoRepairGenerationService autoRepairGenerationService,
            RunEventStreamService streamService
    ) {
        this.compileService = compileService;
        this.autoRepairGenerationService = autoRepairGenerationService;
        this.streamService = streamService;
    }

    public void executeAsync(String runId, OrchestratorRunRequest request) {
        CompletableFuture.runAsync(() -> execute(runId, request));
    }

    private void execute(String runId, OrchestratorRunRequest request) {
        String currentStep = null;
        try {
            streamService.emit(RunEvent.of(runId, "RUN_REQUESTED", null, payload("config", request)));
            checkpoint(runId);

            currentStep = "compile";
            streamService.emit(RunEvent.of(runId, "STEP_STARTED", "compile"));
            CompileResponse compiled = compileService.compile(request.getRequirement());
            checkpoint(runId);
            streamService.emit(RunEvent.of(runId, "STEP_SUCCEEDED", "compile", payload(
                    "prompt", compiled.getPrompt(),
                    "dsl", compiled.getDsl()
            )));
            currentStep = null;
            checkpoint(runId);

            currentStep = "generate";
            streamService.emit(RunEvent.of(runId, "STEP_STARTED", "generate"));
            AutoRepairGenerationResponse generation = autoRepairGenerationService.generateAndRepair(
                    buildGenerationRequest(request, compiled.getPrompt())
            );
            checkpoint(runId);
            OrchestratorRunResponse response = new OrchestratorRunResponse(
                    runId,
                    compiled.getDsl(),
                    compiled.getPrompt(),
                    generation,
                    Instant.now()
            );
            streamService.emit(RunEvent.of(runId, "STEP_SUCCEEDED", "generate", payload(
                    "generation", generation,
                    "output", generation.getFinalOutput()
            )));
            currentStep = null;
            checkpoint(runId);

            currentStep = "verify";
            emitVerificationAndRepair(runId, generation);
            currentStep = null;
            checkpoint(runId);

            if (generation.isSuccessful()) {
                streamService.emit(RunEvent.of(runId, "RUN_COMPLETED", null, payload("result", response)));
            } else {
                streamService.emit(RunEvent.of(runId, "RUN_FAILED", null, payload(
                        "error", "Run finished with status: " + generation.getStatus(),
                        "result", response
                )));
            }
        } catch (CancelledRunException ignored) {
            // RUN_CANCELLED is emitted by the control stream.
        } catch (Exception ex) {
            if (currentStep != null) {
                streamService.emit(RunEvent.of(runId, "STEP_FAILED", currentStep, payload("error", ex.getMessage())));
            }
            streamService.emit(RunEvent.of(runId, "RUN_FAILED", null, payload("error", ex.getMessage())));
        }
    }

    private void checkpoint(String runId) {
        if (streamService.isCancelled(runId)) {
            throw new CancelledRunException();
        }
        streamService.awaitIfPaused(runId);
        if (streamService.isCancelled(runId)) {
            throw new CancelledRunException();
        }
    }

    private void emitVerificationAndRepair(String runId, AutoRepairGenerationResponse generation) {
        streamService.emit(RunEvent.of(runId, "STEP_STARTED", "verify"));
        List<AutoRepairAttempt> attempts = generation.getAttempts();
        List<AutoRepairAttempt> repairAttempts = attempts == null || attempts.size() <= 1
                ? List.of()
                : attempts.subList(1, attempts.size());

        if (!repairAttempts.isEmpty()) {
            streamService.emit(RunEvent.of(runId, "STEP_FAILED", "verify", payload(
                    "error", getAttemptSummary(attempts.get(0)),
                    "verification", attempts.get(0).getVerificationResult()
            )));
            streamService.emit(RunEvent.of(runId, "STEP_STARTED", "repair", payload(
                    "rounds", repairAttempts.size()
            )));
            streamService.emit(RunEvent.of(
                    runId,
                    generation.isSuccessful() ? "STEP_SUCCEEDED" : "STEP_FAILED",
                    "repair",
                    payload(
                            "error", generation.isSuccessful() ? null : getVerificationSummary(generation),
                            "summary", getVerificationSummary(generation),
                            "attempts", repairAttempts
                    )
            ));
            return;
        }

        streamService.emit(RunEvent.of(
                runId,
                generation.isSuccessful() ? "STEP_SUCCEEDED" : "STEP_FAILED",
                "verify",
                payload(
                        "error", generation.isSuccessful() ? null : getVerificationSummary(generation),
                        "summary", getVerificationSummary(generation),
                        "verification", generation.getFinalVerificationResult()
                )
        ));
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

    private String getAttemptSummary(AutoRepairAttempt attempt) {
        if (attempt == null || attempt.getVerificationResult() == null) {
            return "Verification failed.";
        }
        return attempt.getVerificationResult().getSummary();
    }

    private String getVerificationSummary(AutoRepairGenerationResponse generation) {
        if (generation.getFinalVerificationResult() == null) {
            return generation.getStatus();
        }
        return generation.getFinalVerificationResult().getSummary();
    }

    private Map<String, Object> payload(Object... pairs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (int index = 0; index + 1 < pairs.length; index += 2) {
            Object value = pairs[index + 1];
            if (value != null) {
                payload.put(String.valueOf(pairs[index]), value);
            }
        }
        return payload;
    }

    private static final class CancelledRunException extends RuntimeException {
    }
}
