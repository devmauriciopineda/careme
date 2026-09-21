package com.careme.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class AgentOperationTest {

    @Test
    void offersExactlyTheOperationsTheAssistantMayAskFor() {
        assertThat(AgentOperation.values())
                .containsExactlyInAnyOrder(
                        AgentOperation.CONSULT_HISTORY,
                        AgentOperation.CONSULT_MEASUREMENTS,
                        AgentOperation.RECORD_NOTE,
                        AgentOperation.RECORD_MEASUREMENT);
    }

    @Test
    void doesNotOfferTheRetiredFactTypeToTheAssistant() {
        assertThat(typeEnumOf(AgentOperation.RECORD_NOTE))
                .containsExactlyInAnyOrder("diagnosis", "medication", "note");
        assertThat(typeEnumOf(AgentOperation.CONSULT_HISTORY))
                .containsExactlyInAnyOrder("diagnosis", "medication", "note");
    }

    @SuppressWarnings("unchecked")
    private static java.util.List<String> typeEnumOf(AgentOperation operation) {
        java.util.Map<String, Object> properties =
                (java.util.Map<String, Object>) operation.arguments().get("properties");
        java.util.Map<String, Object> type = (java.util.Map<String, Object>) properties.get("type");
        return (java.util.List<String>) type.get("enum");
    }

    @Test
    void namesEveryOperationDifferently() {
        assertThat(Arrays.stream(AgentOperation.values()).map(AgentOperation::operationName).distinct())
                .hasSameSizeAs(AgentOperation.values());
    }

    @Test
    void resolvesADeclaredOperationByItsName() {
        assertThat(AgentOperation.byName("consult_history")).contains(AgentOperation.CONSULT_HISTORY);
        assertThat(AgentOperation.byName(" record_note ")).contains(AgentOperation.RECORD_NOTE);
    }

    @Test
    void refusesANameThatIsNotOneOfTheAvailableOperations() {
        assertThat(AgentOperation.byName("delete_event")).isEmpty();
        assertThat(AgentOperation.byName("")).isEmpty();
        assertThat(AgentOperation.byName("   ")).isEmpty();
        assertThat(AgentOperation.byName(null)).isEmpty();
    }

    @Test
    void declaresNoStorageDetailForAnyOperation() {
        assertThat(AgentOperation.values()).allSatisfy(operation -> {
            assertThat(operation.operationName()).doesNotContain("path", "file", "data/");
            assertThat(operation.description()).doesNotContain("data/", ".md", "filesystem", "Markdown");
            assertThat(operation.arguments().toString()).doesNotContain("data/", ".md", "filesystem", "path");
        });
    }

    @Test
    void describesEveryArgumentOfEveryOperation() {
        assertThat(AgentOperation.values()).allSatisfy(operation -> {
            assertThat(operation.arguments()).containsKeys("type", "properties", "required");
            assertThat(operation.arguments().get("properties")).isInstanceOf(java.util.Map.class);
            assertThat(operation.arguments().get("required")).isInstanceOf(java.util.List.class);
        });
    }
}
