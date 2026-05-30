package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/graph/playbooks")
public class PlaybookTemplateController {

    private final PlaybookTemplateService playbookTemplateService;

    public PlaybookTemplateController(PlaybookTemplateService playbookTemplateService) {
        this.playbookTemplateService = playbookTemplateService;
    }

    @GetMapping
    public ApiResponse<List<PlaybookTemplate>> list() {
        return ApiResponse.ok("playbooks loaded", playbookTemplateService.list());
    }

    @GetMapping("/{id}")
    public ApiResponse<PlaybookTemplate> get(@PathVariable String id) {
        return ApiResponse.ok(
                "playbook loaded",
                playbookTemplateService.find(id)
                        .orElseThrow(() -> new IllegalArgumentException("Playbook not found: " + id))
        );
    }
}
