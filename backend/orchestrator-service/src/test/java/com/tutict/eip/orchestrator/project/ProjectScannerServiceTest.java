package com.tutict.eip.orchestrator.project;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ProjectScannerServiceTest {

    @TempDir
    Path tempDir;

    @Test
    void scansApiRoutesDocumentsAndBusinessCapabilities() throws IOException {
        write(
                "backend/customer-service/pom.xml",
                "<project><artifactId>customer-service</artifactId></project>"
        );
        write(
                "backend/customer-service/src/main/java/com/acme/CustomerController.java",
                """
                        package com.acme;

                        import org.springframework.web.bind.annotation.GetMapping;
                        import org.springframework.web.bind.annotation.RequestMapping;
                        import org.springframework.web.bind.annotation.RestController;

                        @RestController
                        @RequestMapping("/api/customers")
                        class CustomerController {
                            @GetMapping("/{id}")
                            String getCustomer() {
                                return "ok";
                            }
                        }
                        """
        );
        write(
                "enterprise-insight-backend-react/package.json",
                "{\"scripts\":{\"build\":\"vite build\"}}"
        );
        write(
                "enterprise-insight-backend-react/src/App.tsx",
                """
                        import { Route } from 'react-router-dom'

                        export function App() {
                          return <Route path="/customers" element={<div />} />
                        }
                        """
        );
        write("docs/customer-discovery.md", "# Customer discovery");
        write(
                "backend/customer-service/src/test/java/com/acme/CustomerControllerTest.java",
                "class CustomerControllerTest {}"
        );

        ProjectScannerService scanner = new ProjectScannerService(properties());

        ProjectInventory inventory = scanner.scanCurrentProject();

        assertThat(inventory.summary().apiEndpointCount()).isEqualTo(1);
        assertThat(inventory.summary().frontendRouteCount()).isEqualTo(1);
        assertThat(inventory.summary().documentCount()).isEqualTo(1);
        assertThat(inventory.summary().testCount()).isEqualTo(1);
        assertThat(inventory.modules())
                .extracting(ProjectInventory.ProjectModule::path)
                .contains("backend/customer-service", "enterprise-insight-backend-react", "docs");
        assertThat(inventory.apiEndpoints().getFirst().path()).isEqualTo("/api/customers/{id}");
        assertThat(inventory.frontendRoutes().getFirst().path()).isEqualTo("/customers");
        assertThat(inventory.businessCapabilities())
                .extracting(ProjectInventory.BusinessCapability::name)
                .contains("customers");
        assertThat(inventory.deliveryOpportunities()).isNotEmpty();
    }

    @Test
    void createsDeliveryBriefFromProjectEvidence() throws IOException {
        write(
                "backend/customer-service/pom.xml",
                "<project><artifactId>customer-service</artifactId></project>"
        );
        write(
                "backend/customer-service/src/main/java/com/acme/CustomerController.java",
                """
                        import org.springframework.web.bind.annotation.GetMapping;
                        import org.springframework.web.bind.annotation.RequestMapping;

                        @RequestMapping("/api/customers")
                        class CustomerController {
                            @GetMapping("/{id}")
                            String getCustomer() {
                                return "ok";
                            }
                        }
                        """
        );
        write("docs/customer-discovery.md", "# Customer discovery");

        ProjectScannerService scanner = new ProjectScannerService(properties());

        ProjectDeliveryBrief brief = scanner.createDeliveryBrief();

        assertThat(brief.playbookId()).isEqualTo("industry-business-discovery");
        assertThat(brief.targetDirectory()).isEqualTo("generated-fde-delivery");
        assertThat(brief.verifyCommands()).containsExactly(List.of("mvn", "test"));
        assertThat(brief.requirement())
                .contains("FDE delivery brief")
                .contains("GET /api/customers/{id}")
                .contains("backend/customer-service/src/main/java/com/acme/CustomerController.java");
        assertThat(brief.options()).containsEntry("source", "project-analysis");
        assertThat(brief.evidence()).extracting(ProjectInventory.CodeEvidence::kind).contains("api", "document");
    }

    private ProjectAnalysisProperties properties() {
        ProjectAnalysisProperties properties = new ProjectAnalysisProperties();
        properties.setRoot(tempDir.toString());
        properties.setMaxDepth(8);
        properties.setMaxFiles(200);
        properties.setMaxEvidencePerCategory(20);
        return properties;
    }

    private void write(String relativePath, String content) throws IOException {
        Path target = tempDir.resolve(relativePath);
        Files.createDirectories(target.getParent());
        Files.writeString(target, content);
    }
}
