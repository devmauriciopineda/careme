package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.ChatMessageResponse;
import org.junit.jupiter.api.Test;

class ClinicalAnswerResultTest {

    @Test
    void normalizesNullCollectionsAndPreservesAnswerKinds() {
        var answered = ClinicalAnswerResult.answered(null, "Respuesta");
        var failure = ClinicalAnswerResult.failure("Falló");
        var noRecords = ClinicalAnswerResult.noRecords(
                ChatMessageResponse.AbsenceReason.EMPTY_HISTORY, null, "Sin registros");

        assertThat(answered.kind()).isEqualTo(ClinicalAnswerResult.Kind.ANSWERED);
        assertThat(answered.events()).isEmpty();
        assertThat(failure.kind()).isEqualTo(ClinicalAnswerResult.Kind.FAILURE);
        assertThat(noRecords.kind()).isEqualTo(ClinicalAnswerResult.Kind.NO_RECORDS);
        assertThat(noRecords.suggestedActions()).isEmpty();
    }
}