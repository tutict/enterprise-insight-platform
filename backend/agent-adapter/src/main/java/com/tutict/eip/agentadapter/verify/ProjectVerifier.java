package com.tutict.eip.agentadapter.verify;

import com.tutict.eip.agentadapter.domain.VerificationResult;

import java.util.List;

public interface ProjectVerifier {

    VerificationResult verify(String targetDirectory, List<List<String>> commands);
}
