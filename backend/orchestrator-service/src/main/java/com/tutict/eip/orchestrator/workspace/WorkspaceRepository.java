package com.tutict.eip.orchestrator.workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository {

    Workspace save(WorkspaceRequest request);

    Workspace ensureDefaultWorkspace();

    Optional<Workspace> find(String workspaceId);

    List<Workspace> list();
}
