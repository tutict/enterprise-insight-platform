package com.tutict.eip.agentadapter.storage;

import com.tutict.eip.agentadapter.domain.ProjectWriteResult;

public interface ProjectFileWriter {

    ProjectWriteResult writeProject(String targetDirectory, String generatedOutput);
}
