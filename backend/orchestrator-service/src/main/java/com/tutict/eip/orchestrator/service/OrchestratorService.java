package com.tutict.eip.orchestrator.service;

import com.tutict.eip.orchestrator.domain.OrchestratorRunRequest;
import com.tutict.eip.orchestrator.domain.OrchestratorRunResponse;

public interface OrchestratorService {

    OrchestratorRunResponse run(OrchestratorRunRequest request);
}
