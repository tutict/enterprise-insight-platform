package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.domain.AgentExecutionRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairAttempt;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;
import com.tutict.eip.agentadapter.domain.OllamaGenerationResult;
import com.tutict.eip.agentadapter.domain.ProjectWriteResult;
import com.tutict.eip.agentadapter.domain.VerificationResult;
import com.tutict.eip.agentadapter.provider.AgentAdapter;
import com.tutict.eip.agentadapter.storage.ProjectFileWriter;
import com.tutict.eip.agentadapter.verify.ProjectVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultAutoRepairGenerationService implements AutoRepairGenerationService {

    private static final Logger log = LoggerFactory.getLogger(DefaultAutoRepairGenerationService.class);

    private final AgentAdapter agentAdapter;
    private final ProjectFileWriter projectFileWriter;
    private final ProjectVerifier projectVerifier;

    public DefaultAutoRepairGenerationService(
            AgentAdapter agentAdapter,
            ProjectFileWriter projectFileWriter,
            ProjectVerifier projectVerifier
    ) {
        this.agentAdapter = agentAdapter;
        this.projectFileWriter = projectFileWriter;
        this.projectVerifier = projectVerifier;
    }

    @Override
    public AutoRepairGenerationResponse generateAndRepair(AutoRepairGenerationRequest request) {
        int maxAttempts = request.getMaxRepairRounds() + 1;
        List<AutoRepairAttempt> attempts = new ArrayList<>();
        String prompt = ensureFileFormatInstruction(request.getPrompt());
        String finalOutput = "";
        VerificationResult finalVerification = null;
        ProjectWriteResult lastWriteResult = null;

        log.info("Starting auto-repair generation targetDirectory={} maxAttempts={} provider={}",
                request.getTargetDirectory(), maxAttempts, agentAdapter.provider());

        for (int attemptNumber = 1; attemptNumber <= maxAttempts; attemptNumber++) {
            log.info("Auto-repair attempt started attemptNumber={} targetDirectory={}",
                    attemptNumber, request.getTargetDirectory());
            OllamaGenerationResult generationResult = agentAdapter.generate(buildAgentRequest(request, prompt), ignored -> {
            });
            finalOutput = generationResult.getContent();
            lastWriteResult = projectFileWriter.writeProject(request.getTargetDirectory(), finalOutput);
            finalVerification = projectVerifier.verify(request.getTargetDirectory(), request.getVerifyCommands());
            boolean successful = finalVerification.isSuccessful();

            attempts.add(new AutoRepairAttempt(
                    attemptNumber,
                    successful,
                    prompt,
                    finalOutput,
                    lastWriteResult.getFiles(),
                    finalVerification
            ));

            if (successful) {
                log.info("Auto-repair generation succeeded attemptNumber={} targetDirectory={}",
                        attemptNumber, request.getTargetDirectory());
                return new AutoRepairGenerationResponse(
                        true,
                        "VERIFIED",
                        lastWriteResult.getProjectRoot(),
                        attempts.size(),
                        finalOutput,
                        finalVerification,
                        attempts
                );
            }

            if (attemptNumber == maxAttempts) {
                log.warn("Auto-repair generation stopped after max attempts targetDirectory={} attempts={}",
                        request.getTargetDirectory(), attempts.size());
                break;
            }

            prompt = buildRepairPrompt(request.getPrompt(), finalOutput, finalVerification, attemptNumber, maxAttempts);
            log.info("Auto-repair feedback prompt prepared nextAttempt={} targetDirectory={}",
                    attemptNumber + 1, request.getTargetDirectory());
        }

        return new AutoRepairGenerationResponse(
                false,
                "FAILED_AFTER_MAX_REPAIR_ROUNDS",
                lastWriteResult == null ? null : lastWriteResult.getProjectRoot(),
                attempts.size(),
                finalOutput,
                finalVerification,
                attempts
        );
    }

    private AgentExecutionRequest buildAgentRequest(AutoRepairGenerationRequest request, String prompt) {
        AgentExecutionRequest agentRequest = new AgentExecutionRequest();
        agentRequest.setModel(request.getModel());
        agentRequest.setHarnessPrompt(prompt);
        agentRequest.setTargetPath(request.getTargetDirectory());
        agentRequest.setOptions(request.getOptions());
        return agentRequest;
    }

    private String ensureFileFormatInstruction(String prompt) {
        return prompt
                + "\n\n# OUTPUT FORMAT\n"
                + "Return the full project as file blocks only. Use this exact format for every file:\n"
                + "===FILE START===\n"
                + "relative/path/from/project/root\n"
                + "complete file content\n"
                + "===FILE END===";
    }

    private String buildRepairPrompt(
            String originalPrompt,
            String previousOutput,
            VerificationResult verificationResult,
            int previousAttempt,
            int maxAttempts
    ) {
        return ensureFileFormatInstruction(originalPrompt)
                + "\n\n# REPAIR CONTEXT\n"
                + "The previous generated project failed verification.\n"
                + "Previous attempt: " + previousAttempt + " of " + maxAttempts + "\n"
                + "Fix the project and return the complete corrected project, not a patch.\n\n"
                + "# VERIFICATION ERROR\n"
                + verificationResult.getSummary()
                + "\n\n# PREVIOUS OUTPUT\n"
                + previousOutput;
    }
}
