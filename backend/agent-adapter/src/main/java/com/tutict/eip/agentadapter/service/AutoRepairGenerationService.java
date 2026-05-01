package com.tutict.eip.agentadapter.service;

import com.tutict.eip.agentadapter.domain.AutoRepairGenerationRequest;
import com.tutict.eip.agentadapter.domain.AutoRepairGenerationResponse;

public interface AutoRepairGenerationService {

    AutoRepairGenerationResponse generateAndRepair(AutoRepairGenerationRequest request);
}
