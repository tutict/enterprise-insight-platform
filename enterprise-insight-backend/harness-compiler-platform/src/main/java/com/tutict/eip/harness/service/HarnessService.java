package com.tutict.eip.harness.service;

import com.tutict.eip.harness.domain.HarnessRunRequest;
import com.tutict.eip.harness.domain.HarnessRunResponse;

public interface HarnessService {

    HarnessRunResponse run(HarnessRunRequest request);
}
