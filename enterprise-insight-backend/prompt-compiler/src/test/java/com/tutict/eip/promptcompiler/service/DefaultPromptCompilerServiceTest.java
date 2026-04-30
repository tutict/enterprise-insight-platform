package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.PromptCompileRequest;
import com.tutict.eip.promptcompiler.domain.PromptCompileResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DefaultPromptCompilerServiceTest {

    @Autowired
    private PromptCompilerService promptCompilerService;

    @Test
    void compileGeneratesHarnessPromptFromYamlDsl() {
        String yaml = """
                project:
                  type: spring_boot
                  modules:
                    - user
                    - auth
                    - leaderboard
                constraints:
                  db: mysql
                """;

        PromptCompileResponse response = promptCompilerService.compile(new PromptCompileRequest(yaml, null));

        assertThat(response.getDslDocument().getProject().getType()).isEqualTo("spring_boot");
        assertThat(response.getDslDocument().getProject().getModules()).containsExactly("user", "auth", "leaderboard");
        assertThat(response.getCompiledPrompt().getTemplateName()).isEqualTo("harness-default");
        assertThat(response.getCompiledPrompt().getSections())
                .containsExactly("ROLE", "GOAL", "MODULES", "CONSTRAINTS", "OUTPUT FORMAT");
        assertThat(response.getCompiledPrompt().getHarnessPrompt()).isEqualTo("""
                # ROLE
                You are an AI Harness coding agent that generates production-ready Java and Spring Boot code.
                
                # GOAL
                Convert the structured DSL into a complete, compilable implementation plan and code files.
                
                # MODULES
                - project_type: spring_boot
                - module: user
                - module: auth
                - module: leaderboard
                
                # CONSTRAINTS
                - db: mysql
                
                # OUTPUT FORMAT
                Return only generated files using this exact format:
                ===FILE START===
                relative/path/from/project/root
                complete file content
                ===FILE END===""");
    }

    @Test
    void compileProducesStablePromptForSameInput() {
        String yaml = """
                project:
                  type: spring_boot
                  modules:
                    - user
                    - auth
                constraints:
                  db: mysql
                """;

        PromptCompileResponse first = promptCompilerService.compile(new PromptCompileRequest(yaml, null));
        PromptCompileResponse second = promptCompilerService.compile(new PromptCompileRequest(yaml, null));

        assertThat(first.getCompiledPrompt().getHarnessPrompt())
                .isEqualTo(second.getCompiledPrompt().getHarnessPrompt());
        assertThat(first.getCompiledPrompt().getCompiledAt())
                .isEqualTo(second.getCompiledPrompt().getCompiledAt());
    }
}
