package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.entity.PatientProfileUpdate;
import org.junit.jupiter.api.Test;

class AgentOperationResultTest {

    private static final PatientProfileUpdate UPDATE =
            new PatientProfileUpdate("Ana", null, null, null, null, null, null, true);

    @Test
    void reportsACollectedProfileAsACompletedOperation() {
        AgentOperationResult result = AgentOperationResult.of(
                AgentOperation.RECORD_PROFILE,
                new PatientProfileResult(PatientProfileResult.Kind.COLLECTED, UPDATE, "Lo he anotado."));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.message()).isEqualTo("Lo he anotado.");
        assertThat(result.payload()).containsEntry("confirmed", true);
        assertThat(result.profile()).isNotNull();
        assertThat(result.completed()).isTrue();
    }

    @Test
    void reportsADuplicateProfileAsCompletedWithoutClaimingItWasCollected() {
        AgentOperationResult result = AgentOperationResult.of(
                AgentOperation.RECORD_PROFILE,
                new PatientProfileResult(PatientProfileResult.Kind.DUPLICATE, UPDATE, "Ya estaba anotado."));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.payload()).containsEntry("confirmed", true);
    }

    @Test
    void reportsAFailedProfileAsAFailure() {
        AgentOperationResult result = AgentOperationResult.of(
                AgentOperation.RECORD_PROFILE,
                new PatientProfileResult(PatientProfileResult.Kind.FAILURE, null, "No he podido guardarlo."));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.FAILED);
        assertThat(result.payload()).containsEntry("confirmed", false);
        assertThat(result.completed()).isFalse();
    }

    @Test
    void rejectsAnOperationByNamingWhyItCouldNotRun() {
        AgentOperationResult rejected = AgentOperationResult.rejected(
                AgentOperationResult.Rejection.OUT_OF_REACH, "No está a mi alcance.");

        assertThat(rejected.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        assertThat(rejected.rejection()).isEqualTo(AgentOperationResult.Rejection.OUT_OF_REACH);
        assertThat(rejected.completed()).isFalse();
        assertThat(rejected.payload()).isEmpty();
    }
}
