package com.tutict.eip.orchestrator.graph.runtime;

import com.tutict.eip.orchestrator.graph.model.GraphDefinition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GraphExecutorTest {

    @Test
    void executesConditionLoopAndEmitsTerminalEvent() {
        CapturingGraphEventStreamService streamService = new CapturingGraphEventStreamService();
        GraphExecutor executor = new GraphExecutor(streamService);
        GraphDefinition graph = DefaultGraphDefinitions.compileGenerateVerifyRepair(3, 1);

        executor.execute("run-1", graph);

        assertThat(streamService.events)
                .extracting(GraphEvent::getType)
                .containsSubsequence(
                        "GRAPH_RUN_STARTED",
                        "NODE_STARTED",
                        "NODE_SUCCEEDED",
                        "EDGE_TRAVERSED"
                )
                .contains("NODE_FAILED", "GRAPH_RUN_COMPLETED");
        assertThat(streamService.events)
                .anySatisfy(event -> {
                    assertThat(event.getType()).isEqualTo("EDGE_TRAVERSED");
                    assertThat(event.getEdgeId()).isEqualTo("verify-repair");
                })
                .anySatisfy(event -> {
                    assertThat(event.getType()).isEqualTo("EDGE_TRAVERSED");
                    assertThat(event.getEdgeId()).isEqualTo("repair-generate");
                });
        assertThat(streamService.events.getLast().getType()).isEqualTo("GRAPH_RUN_COMPLETED");
    }

    private static final class CapturingGraphEventStreamService extends GraphEventStreamService {
        private final List<GraphEvent> events = new ArrayList<>();

        @Override
        public void emit(GraphEvent event) {
            events.add(event);
        }
    }
}
