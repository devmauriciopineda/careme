package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Verifies the offline composer, which is the default mode and never reaches the network. */
class FakeClinicalAnswerComposerTest {

    @Test
    void answersWithTheRetrievedContentAndCitesEveryEvent() {
        var composed = new FakeClinicalAnswerComposer("full", "")
                .compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión"), event("evt_002", "Enalapril")));

        assertThat(composed.text()).isEqualTo("Hipertensión; Enalapril");
        assertThat(composed.references()).containsExactly("evt_001", "evt_002");
        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.FULL);
        assertThat(composed.unsupported()).as("la pregunta quedó cubierta por completo").isNull();
    }

    @Test
    void reportsAConfiguredUnsupportedPartSoPartialAnswersAreTestableOffline() {
        var composed = new FakeClinicalAnswerComposer("partial", "lo que pasó el año pasado")
                .compose("¿Cuándo me la diagnosticaron y qué pasó el año pasado?",
                        List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.text()).isEqualTo("Hipertensión");
        assertThat(composed.references()).containsExactly("evt_001");
        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.PARTIAL);
        assertThat(composed.unsupported()).isEqualTo("lo que pasó el año pasado");
    }

    @Test
    void reportsNoneWhenTheRetrievedEventsDoNotAnswerSoTheAbsenceIsTestableOffline() {
        var composed = new FakeClinicalAnswerComposer("none", "")
                .compose("¿He tenido migrañas?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.NONE);
        assertThat(composed.references()).as("ningún hecho sostiene una respuesta").isEmpty();
        assertThat(composed.unsupported()).isNull();
    }

    @Test
    void downgradesAPartialRequestWithoutAPartToDeclare() {
        var composed = new FakeClinicalAnswerComposer("partial", "")
                .compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.FULL);
        assertThat(composed.unsupported()).isNull();
    }

    @Test
    void refusesToComposeFromAnEmptySet() {
        assertThatThrownBy(() -> new FakeClinicalAnswerComposer("full", "").compose("¿Cuándo?", List.of()))
                .isInstanceOf(LlmIntegrationException.class)
                .hasMessage("There is nothing to answer with");
        assertThatThrownBy(() -> new FakeClinicalAnswerComposer("full", "").compose("¿Cuándo?", null))
            .isInstanceOf(LlmIntegrationException.class)
            .hasMessage("There is nothing to answer with");
        }

        @Test
        void treatsNullConfigurationAsFullCoverage() {
        var composed = new FakeClinicalAnswerComposer(null, null)
            .compose("¿Cuándo?", List.of(event("evt_001", "Hipertensión")));

        assertThat(composed.coverage()).isEqualTo(ClinicalAnswerComposer.Coverage.FULL);
    }

    private static ClinicalEvent event(String code, String content) {
        return new ClinicalEvent(
                UUID.randomUUID(),
                code,
                ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                LocalDate.of(2026, 1, 10),
                ClinicalEvent.DatePrecision.EXACT,
                "el 10 de enero",
                content,
                ClinicalEvent.EventSource.PATIENT,
                OffsetDateTime.now(ZoneOffset.UTC));
    }
}
