package com.tutict.eip.ai.rag;

import com.tutict.eip.common.ApiResponse;
import com.tutict.eip.common.security.RequireRoles;
import com.tutict.eip.common.security.RoleConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class IngestController {

    private final RagService ragService;

    public IngestController(RagService ragService) {
        this.ragService = ragService;
    }

    @PostMapping("/ingest")
    @RequireRoles({RoleConstants.ADMIN})
    public ApiResponse<RagService.IngestResult> ingest(@Valid @RequestBody IngestRequest request) {
        List<RagService.IngestDocument> docs = request.documents().stream()
                .map(doc -> new RagService.IngestDocument(doc.id(), doc.title(), doc.content(), doc.metadata()))
                .toList();
        return ApiResponse.ok("ingest ok", ragService.ingest(docs));
    }

    public record IngestRequest(@NotEmpty List<@Valid IngestDocument> documents) {
    }

    public record IngestDocument(String id, String title, @NotBlank String content, Map<String, Object> metadata) {
    }
}
