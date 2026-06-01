package com.tutict.eip.orchestrator.project;

import com.tutict.eip.orchestrator.controller.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProjectAnalysisControllerTest {

    @TempDir
    Path tempDir;

    @Test
    void currentEndpointReturnsProjectInventory() throws Exception {
        write("backend/orchestrator-service/pom.xml", "<project />");
        write(
                "backend/orchestrator-service/src/main/java/com/acme/RunController.java",
                """
                        import org.springframework.web.bind.annotation.GetMapping;
                        import org.springframework.web.bind.annotation.RequestMapping;

                        @RequestMapping("/api/runs")
                        class RunController {
                          @GetMapping
                          String list() { return "ok"; }
                        }
                        """
        );

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProjectAnalysisController(new ProjectScannerService(properties())))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/project-analysis/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.summary.apiEndpointCount").value(1))
                .andExpect(jsonPath("$.data.apiEndpoints[0].path").value("/api/runs"));
    }

    @Test
    void deliveryBriefEndpointReturnsRunReadyRequirement() throws Exception {
        write("backend/orchestrator-service/pom.xml", "<project />");
        write(
                "backend/orchestrator-service/src/main/java/com/acme/RunController.java",
                """
                        import org.springframework.web.bind.annotation.GetMapping;
                        import org.springframework.web.bind.annotation.RequestMapping;

                        @RequestMapping("/api/runs")
                        class RunController {
                          @GetMapping
                          String list() { return "ok"; }
                        }
                        """
        );

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ProjectAnalysisController(new ProjectScannerService(properties())))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/api/project-analysis/current/delivery-brief"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.playbookId").value("industry-business-discovery"))
                .andExpect(jsonPath("$.data.requirement").value(org.hamcrest.Matchers.containsString("GET /api/runs")));
    }

    private ProjectAnalysisProperties properties() {
        ProjectAnalysisProperties properties = new ProjectAnalysisProperties();
        properties.setRoot(tempDir.toString());
        return properties;
    }

    private void write(String relativePath, String content) throws IOException {
        Path target = tempDir.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
    }
}
