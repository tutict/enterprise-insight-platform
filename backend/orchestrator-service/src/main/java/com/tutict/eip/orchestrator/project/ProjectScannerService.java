package com.tutict.eip.orchestrator.project;

import com.tutict.eip.orchestrator.project.ProjectInventory.ApiEndpointEvidence;
import com.tutict.eip.orchestrator.project.ProjectInventory.BusinessCapability;
import com.tutict.eip.orchestrator.project.ProjectInventory.CodeEvidence;
import com.tutict.eip.orchestrator.project.ProjectInventory.DeliveryOpportunity;
import com.tutict.eip.orchestrator.project.ProjectInventory.FrontendRouteEvidence;
import com.tutict.eip.orchestrator.project.ProjectInventory.ProjectModule;
import com.tutict.eip.orchestrator.project.ProjectInventory.ProjectSummary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProjectScannerService {

    private static final Pattern JAVA_MAPPING = Pattern.compile(
            "@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\\s*(?:\\(([^)]*)\\))?"
    );
    private static final Pattern QUOTED_VALUE = Pattern.compile("\"([^\"]*)\"");
    private static final Pattern REQUEST_METHOD = Pattern.compile("RequestMethod\\.([A-Z]+)");
    private static final Pattern ROUTE_PATH = Pattern.compile("path\\s*=\\s*\"([^\"]+)\"");

    private final ProjectAnalysisProperties properties;

    public ProjectScannerService(ProjectAnalysisProperties properties) {
        this.properties = properties;
    }

    public ProjectInventory scanCurrentProject() {
        Path root = resolveRoot();
        List<Path> files = collectFiles(root);
        List<ProjectModule> modules = detectModules(root, files);
        List<ApiEndpointEvidence> apiEndpoints = scanApiEndpoints(root, files);
        List<FrontendRouteEvidence> frontendRoutes = scanFrontendRoutes(root, files);
        List<CodeEvidence> documents = scanDocuments(root, files);
        List<CodeEvidence> tests = scanTests(root, files);
        List<BusinessCapability> capabilities = deriveBusinessCapabilities(apiEndpoints, frontendRoutes, documents);
        List<DeliveryOpportunity> opportunities = deriveDeliveryOpportunities(capabilities, apiEndpoints, frontendRoutes, tests);

        ProjectSummary summary = new ProjectSummary(
                files.size(),
                modules.size(),
                apiEndpoints.size(),
                frontendRoutes.size(),
                capabilities.size(),
                documents.size(),
                tests.size()
        );

        return new ProjectInventory(
                root.toString(),
                Instant.now(),
                summary,
                modules,
                limit(apiEndpoints),
                limit(frontendRoutes),
                limit(capabilities),
                limit(documents),
                limit(tests),
                opportunities
        );
    }

    public ProjectDeliveryBrief createDeliveryBrief() {
        ProjectInventory inventory = scanCurrentProject();
        DeliveryOpportunity opportunity = inventory.deliveryOpportunities().stream()
                .findFirst()
                .orElseGet(() -> defaultOpportunity(inventory));
        List<CodeEvidence> evidence = deliveryBriefEvidence(inventory, opportunity);
        List<List<String>> verifyCommands = defaultVerifyCommands(inventory);
        Instant generatedAt = Instant.now();

        Map<String, Object> options = new LinkedHashMap<>();
        options.put("source", "project-analysis");
        options.put("projectRoot", inventory.rootPath());
        options.put("inventoryGeneratedAt", inventory.generatedAt().toString());
        options.put("briefGeneratedAt", generatedAt.toString());
        options.put("opportunityPriority", opportunity.priority());
        options.put("opportunityTitle", opportunity.title());
        options.put("capabilities", inventory.businessCapabilities().stream()
                .limit(8)
                .map(BusinessCapability::name)
                .toList());
        options.put("evidence", evidence.stream().map(this::evidenceReference).toList());

        return new ProjectDeliveryBrief(
                opportunity.title(),
                opportunity.rationale(),
                buildDeliveryRequirement(inventory, opportunity, evidence),
                "industry-business-discovery",
                "Industry And Business Discovery",
                "generated-fde-delivery",
                verifyCommands,
                2,
                options,
                evidence,
                generatedAt
        );
    }

    private Path resolveRoot() {
        if (properties.getRoot() != null && !properties.getRoot().isBlank()) {
            Path configuredRoot = Path.of(properties.getRoot()).toAbsolutePath().normalize();
            if (!Files.isDirectory(configuredRoot)) {
                throw new IllegalArgumentException("Project analysis root is not a directory: " + configuredRoot);
            }
            return configuredRoot;
        }

        Path current = Path.of("").toAbsolutePath().normalize();
        Path cursor = current;
        while (cursor != null) {
            if (Files.isDirectory(cursor.resolve("backend"))
                    && Files.isDirectory(cursor.resolve("enterprise-insight-backend-react"))) {
                return cursor;
            }
            cursor = cursor.getParent();
        }
        return current;
    }

    private List<Path> collectFiles(Path root) {
        List<Path> files = new ArrayList<>();
        Set<String> ignoredDirectories = new LinkedHashSet<>(properties.getIgnoredDirectories());
        int maxDepth = Math.max(properties.getMaxDepth(), 1);
        int maxFiles = Math.max(properties.getMaxFiles(), 1);

        try {
            Files.walkFileTree(root, Set.of(), maxDepth, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (!root.equals(dir) && ignoredDirectories.contains(dir.getFileName().toString())) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (attrs.isRegularFile() && files.size() < maxFiles && isRelevantFile(file)) {
                        files.add(file);
                    }
                    return files.size() >= maxFiles ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to scan project root: " + root, ex);
        }

        files.sort(Comparator.comparing(path -> relative(root, path)));
        return files;
    }

    private boolean isRelevantFile(Path file) {
        String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".java")
                || name.endsWith(".ts")
                || name.endsWith(".tsx")
                || name.endsWith(".js")
                || name.endsWith(".jsx")
                || name.endsWith(".md")
                || name.endsWith(".yml")
                || name.endsWith(".yaml")
                || name.endsWith(".json")
                || name.equals("pom.xml")
                || name.equals("package.json");
    }

    private List<ProjectModule> detectModules(Path root, List<Path> files) {
        Map<String, List<Path>> filesByModule = new LinkedHashMap<>();
        for (Path file : files) {
            Path relative = root.relativize(file);
            if (relative.getNameCount() == 0) {
                continue;
            }
            String modulePath = modulePath(relative);
            filesByModule.computeIfAbsent(modulePath, ignored -> new ArrayList<>()).add(file);
        }

        return filesByModule.entrySet().stream()
                .filter(entry -> hasModuleMarker(root.resolve(entry.getKey()), entry.getValue()))
                .map(entry -> new ProjectModule(
                        entry.getKey().replace('\\', '/'),
                        entry.getKey().replace('\\', '/'),
                        moduleType(root.resolve(entry.getKey())),
                        entry.getValue().size(),
                        moduleMarkers(root.resolve(entry.getKey()))
                ))
                .sorted(Comparator.comparing(ProjectModule::path))
                .toList();
    }

    private String modulePath(Path relative) {
        if (relative.getNameCount() >= 2 && "backend".equals(relative.getName(0).toString())) {
            return "backend/" + relative.getName(1);
        }
        return relative.getName(0).toString();
    }

    private boolean hasModuleMarker(Path moduleRoot, List<Path> files) {
        return moduleRoot.getFileName().toString().equals("docs")
                || Files.exists(moduleRoot.resolve("pom.xml"))
                || Files.exists(moduleRoot.resolve("package.json"))
                || Files.isDirectory(moduleRoot.resolve("src"))
                || files.stream().anyMatch(file -> file.getFileName().toString().equalsIgnoreCase("README.md"));
    }

    private String moduleType(Path moduleRoot) {
        if (Files.exists(moduleRoot.resolve("package.json"))) {
            return "frontend";
        }
        if (Files.exists(moduleRoot.resolve("pom.xml"))) {
            return "java-service";
        }
        if (moduleRoot.getFileName().toString().equals("docs")) {
            return "documentation";
        }
        return "workspace";
    }

    private List<String> moduleMarkers(Path moduleRoot) {
        List<String> markers = new ArrayList<>();
        for (String marker : List.of("pom.xml", "package.json", "README.md", "src", "docs")) {
            if (Files.exists(moduleRoot.resolve(marker))) {
                markers.add(marker);
            }
        }
        return markers;
    }

    private List<ApiEndpointEvidence> scanApiEndpoints(Path root, List<Path> files) {
        List<ApiEndpointEvidence> endpoints = new ArrayList<>();
        for (Path file : files) {
            if (!file.getFileName().toString().endsWith(".java")) {
                continue;
            }

            List<String> lines = readLines(file);
            String classBasePath = "";
            boolean classSeen = false;
            for (int index = 0; index < lines.size(); index += 1) {
                String line = lines.get(index).trim();
                Matcher matcher = JAVA_MAPPING.matcher(line);
                if (matcher.find()) {
                    String annotation = matcher.group(1);
                    String args = matcher.group(2) == null ? "" : matcher.group(2);
                    String path = extractMappingPath(args);
                    if (!classSeen && "RequestMapping".equals(annotation)) {
                        classBasePath = path;
                    } else {
                        endpoints.add(new ApiEndpointEvidence(
                                httpMethod(annotation, args),
                                joinPaths(classBasePath, path),
                                relative(root, file),
                                index + 1
                        ));
                    }
                }
                if (line.contains(" class ") || line.startsWith("class ") || line.contains(" record ")) {
                    classSeen = true;
                }
            }
        }
        return endpoints;
    }

    private String extractMappingPath(String args) {
        Matcher matcher = QUOTED_VALUE.matcher(args);
        return matcher.find() ? matcher.group(1) : "";
    }

    private String httpMethod(String annotation, String args) {
        return switch (annotation) {
            case "GetMapping" -> "GET";
            case "PostMapping" -> "POST";
            case "PutMapping" -> "PUT";
            case "DeleteMapping" -> "DELETE";
            case "PatchMapping" -> "PATCH";
            default -> {
                Matcher matcher = REQUEST_METHOD.matcher(args);
                yield matcher.find() ? matcher.group(1) : "ANY";
            }
        };
    }

    private String joinPaths(String basePath, String path) {
        String base = normalizeApiPath(basePath);
        String child = normalizeApiPath(path);
        if (base.equals("/")) {
            return child;
        }
        if (child.equals("/")) {
            return base;
        }
        return base + child;
    }

    private String normalizeApiPath(String path) {
        if (path == null || path.isBlank()) {
            return "/";
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        return normalized.replaceAll("/+", "/");
    }

    private List<FrontendRouteEvidence> scanFrontendRoutes(Path root, List<Path> files) {
        List<FrontendRouteEvidence> routes = new ArrayList<>();
        for (Path file : files) {
            String name = file.getFileName().toString();
            if (!name.endsWith(".tsx") && !name.endsWith(".jsx")) {
                continue;
            }
            List<String> lines = readLines(file);
            for (int index = 0; index < lines.size(); index += 1) {
                Matcher matcher = ROUTE_PATH.matcher(lines.get(index));
                while (matcher.find()) {
                    routes.add(new FrontendRouteEvidence(matcher.group(1), relative(root, file), index + 1));
                }
            }
        }
        return routes;
    }

    private List<CodeEvidence> scanDocuments(Path root, List<Path> files) {
        return files.stream()
                .filter(file -> file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                .map(file -> new CodeEvidence("document", file.getFileName().toString(), relative(root, file), 1))
                .toList();
    }

    private List<CodeEvidence> scanTests(Path root, List<Path> files) {
        return files.stream()
                .filter(file -> {
                    String name = file.getFileName().toString();
                    return name.endsWith("Test.java") || name.endsWith(".test.ts") || name.endsWith(".spec.ts")
                            || name.endsWith(".test.tsx") || name.endsWith(".spec.tsx");
                })
                .map(file -> new CodeEvidence("test", file.getFileName().toString(), relative(root, file), 1))
                .toList();
    }

    private List<BusinessCapability> deriveBusinessCapabilities(
            List<ApiEndpointEvidence> apiEndpoints,
            List<FrontendRouteEvidence> frontendRoutes,
            List<CodeEvidence> documents
    ) {
        Map<String, List<CodeEvidence>> evidenceByName = new LinkedHashMap<>();

        for (ApiEndpointEvidence endpoint : apiEndpoints) {
            String name = capabilityFromApiPath(endpoint.path());
            evidenceByName.computeIfAbsent(name, ignored -> new ArrayList<>())
                    .add(new CodeEvidence("api", endpoint.method() + " " + endpoint.path(), endpoint.sourcePath(), endpoint.line()));
        }
        for (FrontendRouteEvidence route : frontendRoutes) {
            String name = capabilityFromRoute(route.path());
            evidenceByName.computeIfAbsent(name, ignored -> new ArrayList<>())
                    .add(new CodeEvidence("frontend-route", route.path(), route.sourcePath(), route.line()));
        }
        for (CodeEvidence document : documents) {
            String name = capabilityFromDocument(document.sourcePath());
            evidenceByName.computeIfAbsent(name, ignored -> new ArrayList<>()).add(document);
        }

        return evidenceByName.entrySet().stream()
                .map(entry -> new BusinessCapability(
                        entry.getKey(),
                        "business-signal",
                        entry.getValue().size(),
                        limit(entry.getValue())
                ))
                .sorted(Comparator.comparing(BusinessCapability::evidenceCount).reversed().thenComparing(BusinessCapability::name))
                .toList();
    }

    private String capabilityFromApiPath(String path) {
        String normalized = path == null ? "" : path.toLowerCase(Locale.ROOT);
        String[] parts = normalized.split("/");
        for (String part : parts) {
            if (!part.isBlank() && !"api".equals(part)) {
                return readableName(part);
            }
        }
        return "api";
    }

    private String capabilityFromRoute(String path) {
        if (path == null || path.isBlank() || "/".equals(path) || "*".equals(path)) {
            return "workspace";
        }
        String[] parts = path.toLowerCase(Locale.ROOT).split("/");
        for (String part : parts) {
            if (!part.isBlank() && !"*".equals(part)) {
                return readableName(part);
            }
        }
        return "workspace";
    }

    private String capabilityFromDocument(String path) {
        String normalized = path.toLowerCase(Locale.ROOT);
        if (normalized.contains("architecture")) {
            return "architecture";
        }
        if (normalized.contains("api")) {
            return "api";
        }
        if (normalized.contains("fde")) {
            return "fde delivery";
        }
        return "documentation";
    }

    private String readableName(String token) {
        return token.replaceAll("[{}:_-]+", " ").trim();
    }

    private List<DeliveryOpportunity> deriveDeliveryOpportunities(
            List<BusinessCapability> capabilities,
            List<ApiEndpointEvidence> apiEndpoints,
            List<FrontendRouteEvidence> frontendRoutes,
            List<CodeEvidence> tests
    ) {
        List<DeliveryOpportunity> opportunities = new ArrayList<>();
        capabilities.stream().findFirst().ifPresent(capability ->
                opportunities.add(new DeliveryOpportunity(
                        "P0",
                        "Anchor FDE discovery on " + capability.name(),
                        "This capability has the strongest code evidence and should be the first candidate for business validation.",
                        capability.evidence()
                ))
        );

        if (!apiEndpoints.isEmpty() && !frontendRoutes.isEmpty()) {
            opportunities.add(new DeliveryOpportunity(
                    "P1",
                    "Map API and frontend routes into current user workflows",
                    "The project exposes both server endpoints and client routes, so workflow mapping can connect user actions to backend capabilities.",
                    List.of(
                            new CodeEvidence("api", apiEndpoints.get(0).method() + " " + apiEndpoints.get(0).path(), apiEndpoints.get(0).sourcePath(), apiEndpoints.get(0).line()),
                            new CodeEvidence("frontend-route", frontendRoutes.get(0).path(), frontendRoutes.get(0).sourcePath(), frontendRoutes.get(0).line())
                    )
            ));
        }

        if (tests.size() < Math.max(3, capabilities.size())) {
            opportunities.add(new DeliveryOpportunity(
                    "P1",
                    "Add verification evidence for high-value workflows",
                    "The scanner found fewer tests than business signals, so FDE delivery should prioritize reproducible verification around urgent workflows.",
                    tests.isEmpty() ? List.of() : List.of(tests.get(0))
            ));
        }

        return opportunities;
    }

    private DeliveryOpportunity defaultOpportunity(ProjectInventory inventory) {
        List<CodeEvidence> evidence = inventory.businessCapabilities().stream()
                .findFirst()
                .map(BusinessCapability::evidence)
                .orElse(List.of());
        return new DeliveryOpportunity(
                "P0",
                "Create an evidence-backed FDE baseline",
                "No stronger candidate was derived, so the first delivery slice should establish project, domain, and verification evidence.",
                evidence
        );
    }

    private List<CodeEvidence> deliveryBriefEvidence(ProjectInventory inventory, DeliveryOpportunity opportunity) {
        Map<String, CodeEvidence> evidenceByKey = new LinkedHashMap<>();
        opportunity.evidence().forEach(evidence -> addEvidence(evidenceByKey, evidence));

        inventory.businessCapabilities().stream()
                .limit(5)
                .flatMap(capability -> capability.evidence().stream().limit(3))
                .forEach(evidence -> addEvidence(evidenceByKey, evidence));
        inventory.apiEndpoints().stream()
                .limit(8)
                .map(endpoint -> new CodeEvidence(
                        "api",
                        endpoint.method() + " " + endpoint.path(),
                        endpoint.sourcePath(),
                        endpoint.line()
                ))
                .forEach(evidence -> addEvidence(evidenceByKey, evidence));
        inventory.frontendRoutes().stream()
                .limit(8)
                .map(route -> new CodeEvidence("frontend-route", route.path(), route.sourcePath(), route.line()))
                .forEach(evidence -> addEvidence(evidenceByKey, evidence));
        inventory.documents().stream()
                .limit(6)
                .forEach(evidence -> addEvidence(evidenceByKey, evidence));
        inventory.tests().stream()
                .limit(6)
                .forEach(evidence -> addEvidence(evidenceByKey, evidence));

        return evidenceByKey.values().stream().limit(30).toList();
    }

    private void addEvidence(Map<String, CodeEvidence> evidenceByKey, CodeEvidence evidence) {
        if (evidence == null) {
            return;
        }
        String key = evidence.kind() + "|" + evidence.sourcePath() + "|" + evidence.line() + "|" + evidence.name();
        evidenceByKey.putIfAbsent(key, evidence);
    }

    private List<List<String>> defaultVerifyCommands(ProjectInventory inventory) {
        boolean hasJavaService = inventory.modules().stream()
                .anyMatch(module -> "java-service".equals(module.type()));
        if (hasJavaService) {
            return List.of(List.of("mvn", "test"));
        }

        boolean hasFrontend = inventory.modules().stream()
                .anyMatch(module -> "frontend".equals(module.type()));
        if (hasFrontend) {
            return List.of(List.of("npm", "run", "build"));
        }

        return List.of(List.of("mvn", "test"));
    }

    private Map<String, Object> evidenceReference(CodeEvidence evidence) {
        Map<String, Object> reference = new LinkedHashMap<>();
        reference.put("kind", evidence.kind());
        reference.put("name", evidence.name());
        reference.put("sourcePath", evidence.sourcePath());
        reference.put("line", evidence.line());
        return reference;
    }

    private String buildDeliveryRequirement(
            ProjectInventory inventory,
            DeliveryOpportunity opportunity,
            List<CodeEvidence> evidence
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append("FDE delivery brief: ").append(opportunity.title()).append("\n\n");
        builder.append("Objective:\n");
        builder.append("- Use existing code evidence to validate the urgent business capability.\n");
        builder.append("- Produce a reproducible delivery slice with implementation notes, verification evidence, and open assumptions.\n\n");

        builder.append("Priority:\n");
        builder.append("- ").append(opportunity.priority()).append(": ").append(opportunity.rationale()).append("\n\n");

        builder.append("Detected project shape:\n");
        builder.append("- Root: ").append(inventory.rootPath()).append("\n");
        builder.append("- Files scanned: ").append(inventory.summary().scannedFiles()).append("\n");
        builder.append("- Modules: ").append(inventory.summary().moduleCount()).append("\n");
        builder.append("- API endpoints: ").append(inventory.summary().apiEndpointCount()).append("\n");
        builder.append("- Frontend routes: ").append(inventory.summary().frontendRouteCount()).append("\n");
        builder.append("- Business capabilities: ").append(inventory.summary().businessCapabilityCount()).append("\n");
        builder.append("- Tests: ").append(inventory.summary().testCount()).append("\n\n");

        List<String> capabilities = inventory.businessCapabilities().stream()
                .limit(8)
                .map(capability -> capability.name() + " (" + capability.evidenceCount() + " evidence)")
                .toList();
        if (!capabilities.isEmpty()) {
            builder.append("Business capability candidates:\n");
            capabilities.forEach(capability -> builder.append("- ").append(capability).append("\n"));
            builder.append("\n");
        }

        if (!evidence.isEmpty()) {
            builder.append("Evidence to ground the work:\n");
            evidence.stream()
                    .limit(18)
                    .forEach(item -> builder
                            .append("- ")
                            .append(item.kind())
                            .append(": ")
                            .append(item.name())
                            .append(" (")
                            .append(item.sourcePath())
                            .append(":")
                            .append(item.line())
                            .append(")\n"));
            builder.append("\n");
        }

        builder.append("Delivery tasks:\n");
        builder.append("1. Confirm the user workflow and business owner assumption behind the selected capability.\n");
        builder.append("2. Map frontend routes, API endpoints, domain/service boundaries, and persistence points for the workflow.\n");
        builder.append("3. Identify the smallest implementation or repair slice that improves the workflow without broad refactoring.\n");
        builder.append("4. Add or adjust executable verification around the workflow and document how to reproduce it.\n");
        builder.append("5. Return changed files, verification commands, risks, and unresolved assumptions.\n\n");

        builder.append("Constraints:\n");
        builder.append("- Preserve existing public API and route contracts unless the evidence proves they are wrong.\n");
        builder.append("- Cite source paths and line numbers for material conclusions.\n");
        builder.append("- Mark unsupported business claims as assumptions.\n");
        builder.append("- Prefer small vertical slices that can become auditable DeliveryRun records.");

        return builder.toString();
    }

    private List<String> readLines(Path file) {
        try {
            return Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException | RuntimeException ignored) {
            return List.of();
        }
    }

    private String relative(Path root, Path file) {
        return root.relativize(file).toString().replace('\\', '/');
    }

    private <T> List<T> limit(List<T> items) {
        return items.stream()
                .limit(Math.max(properties.getMaxEvidencePerCategory(), 1))
                .toList();
    }
}
