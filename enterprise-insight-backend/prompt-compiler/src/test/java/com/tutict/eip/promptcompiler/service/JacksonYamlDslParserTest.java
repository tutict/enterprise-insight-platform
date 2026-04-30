package com.tutict.eip.promptcompiler.service;

import com.tutict.eip.promptcompiler.domain.DslDocument;
import com.tutict.eip.promptcompiler.exception.PromptCompilerException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class JacksonYamlDslParserTest {

    @Autowired
    private DslParser dslParser;

    @Test
    void parseYamlDslToJavaObject() {
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

        DslDocument document = dslParser.parse(yaml);

        assertThat(document.getProject().getType()).isEqualTo("spring_boot");
        assertThat(document.getProject().getModules()).containsExactly("user", "auth", "leaderboard");
        assertThat(document.getConstraints().getDb()).isEqualTo("mysql");
    }

    @Test
    void keepUnknownConstraintFieldsAsExtensions() {
        String yaml = """
                project:
                  type: spring_boot
                  modules:
                    - user
                constraints:
                  db: mysql
                  cache: redis
                """;

        DslDocument document = dslParser.parse(yaml);

        assertThat(document.getConstraints().getExtensions()).containsEntry("cache", "redis");
    }

    @Test
    void rejectYamlWithoutProject() {
        String yaml = """
                constraints:
                  db: mysql
                """;

        assertThatThrownBy(() -> dslParser.parse(yaml))
                .isInstanceOf(PromptCompilerException.class)
                .hasMessageContaining("Invalid DSL document");
    }
}
