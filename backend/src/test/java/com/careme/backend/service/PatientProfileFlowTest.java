package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.careme.backend.dto.AgentOperation;
import com.careme.backend.dto.ChatMessageResponse;
import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import com.careme.backend.entity.PatientProfile;
import com.careme.backend.entity.PatientProfileUpdate;
import com.careme.backend.repository.AdmittedMetricRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PatientProfileFlowTest {

    private static final String CONVERSATION_ID = "conversation-profile";

    @TempDir
    Path root;

    @Test
    void rejectsAnUnconfirmedProfileOperationWithoutCollectingIt() {
        EncounterService encounterService = mock(EncounterService.class);
        AgentOperationExecutor executor = new AgentOperationExecutor(
                mock(ClinicalHistoryQueryService.class), encounterService, new ClinicalEventIntentValidator(),
                new MetricCatalogService(mock(AdmittedMetricRepository.class)), mock(MeasurementQueryService.class));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(AgentOperation.RECORD_PROFILE,
                        new ObjectMapper().createObjectNode().put("name", "Ana").put("confirmed", false)),
                LocalDate.of(2026, 9, 30));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.REJECTED);
        verify(encounterService, never()).collectProfile(anyString(), any());
    }

    @Test
    void collectsAConfirmedProfileOperationWithoutCreatingAClinicalNote() {
        EncounterService encounterService = mock(EncounterService.class);
        when(encounterService.collectProfile(anyString(), any()))
                .thenReturn(new PatientProfileResult(
                        PatientProfileResult.Kind.COLLECTED,
                        new PatientProfileUpdate("Ana", null, null, null, null, null, null, true),
                        "Lo he anotado."));
        AgentOperationExecutor executor = new AgentOperationExecutor(
                mock(ClinicalHistoryQueryService.class), encounterService, new ClinicalEventIntentValidator(),
                new MetricCatalogService(mock(AdmittedMetricRepository.class)), mock(MeasurementQueryService.class));

        AgentOperationResult result = executor.execute(
                CONVERSATION_ID,
                new AgentOperationCall(AgentOperation.RECORD_PROFILE,
                        new ObjectMapper().createObjectNode().put("name", "Ana").put("confirmed", true)),
                LocalDate.of(2026, 9, 30));

        assertThat(result.kind()).isEqualTo(AgentOperationResult.Kind.COMPLETED);
        assertThat(result.profile()).isNotNull();
        verify(encounterService).collectProfile(anyString(), any());
        verify(encounterService, never()).collect(anyString(), any(), any());
    }

    @Test
    void publishesConfirmedProfileValuesOnlyWhenTheConsultationCloses() throws Exception {
        PatientProfileMarkdownStore profileStore = new PatientProfileMarkdownStore(root.resolve("patient"));
        EncounterService service = new EncounterService(
                new EncounterMarkdownStore(root.resolve("encounters")),
                mock(EncounterIndexWriter.class),
                registeredEvents(),
                new ClinicalEventDateNormalizer(),
                emptyMeasurements(),
                profileStore);

        service.collectProfile(CONVERSATION_ID,
                new PatientProfileUpdate("Ana", LocalDate.of(1984, 2, 3), null, null, null, null,
                        new BigDecimal("170"), true));

        assertThat(profileStore.read()).isEqualTo(PatientProfile.empty());
        ChatMessageResponse response = service.close(CONVERSATION_ID);

        assertThat(response.status()).isEqualTo(ChatMessageResponse.Status.NOTHING_TO_REGISTER);
        assertThat(profileStore.read().name()).isEqualTo("Ana");
        assertThat(profileStore.read().dateOfBirth()).isEqualTo(LocalDate.of(1984, 2, 3));
    }

    @Test
    void keepsProfileCandidatesSeparateFromClinicalNotesInOneConsultation() throws Exception {
        ClinicalEventRegistrationService registrationService = registeredEvents();
        PatientProfileMarkdownStore profileStore = new PatientProfileMarkdownStore(root.resolve("patient"));
        EncounterService service = new EncounterService(
                new EncounterMarkdownStore(root.resolve("encounters-mixed")),
                mock(EncounterIndexWriter.class),
                registrationService,
                new ClinicalEventDateNormalizer(),
                emptyMeasurements(),
                profileStore);

        service.collectProfile(CONVERSATION_ID,
                new PatientProfileUpdate("Ana", null, null, null, null, null, null, true));
        service.collect(CONVERSATION_ID,
                new ClinicalEventIntent.Candidate(
                        ClinicalEvent.ClinicalEventType.DIAGNOSIS,
                        "Hipertensión diagnosticada",
                        LocalDate.of(2026, 9, 30),
                        ClinicalEvent.DatePrecision.EXACT,
                        "hoy"),
                LocalDate.of(2026, 9, 30));
        service.close(CONVERSATION_ID);

        assertThat(profileStore.read().name()).isEqualTo("Ana");
        verify(registrationService).register(anyString(), any(), any(), anyString());
        assertThat(profileStore.read().dateOfBirth()).isNull();
    }

    @Test
    void publishesOnlyTheLastConfirmedValueAndLeavesUnknownFieldsAbsent() throws Exception {
        PatientProfileMarkdownStore profileStore = new PatientProfileMarkdownStore(root.resolve("patient-last"));
        EncounterService service = new EncounterService(
                new EncounterMarkdownStore(root.resolve("encounters-last")),
                mock(EncounterIndexWriter.class),
                registeredEvents(),
                new ClinicalEventDateNormalizer(),
                emptyMeasurements(),
                profileStore);

        service.collectProfile(CONVERSATION_ID,
                new PatientProfileUpdate("Ana", null, null, null, null, null, null, true));
        service.collectProfile(CONVERSATION_ID,
                new PatientProfileUpdate("Bea", null, null, null, null, null, null, true));
        service.collectProfile(CONVERSATION_ID,
                new PatientProfileUpdate(null, null, null, null, null, null, null, false));
        service.close(CONVERSATION_ID);

        PatientProfile profile = profileStore.read();
        assertThat(profile.name()).isEqualTo("Bea");
        assertThat(profile.dateOfBirth()).isNull();
        assertThat(profile.heightCm()).isNull();
    }

        @Test
        void retriesProfilePublicationAfterARecoverableCloseFailure() throws Exception {
                FailOnceProfileStore profileStore = new FailOnceProfileStore(root.resolve("patient-retry"));
                EncounterService service = new EncounterService(
                                new EncounterMarkdownStore(root.resolve("encounters-retry")),
                                mock(EncounterIndexWriter.class),
                                registeredEvents(),
                                new ClinicalEventDateNormalizer(),
                                emptyMeasurements(),
                                profileStore);
                service.collectProfile(CONVERSATION_ID,
                                new PatientProfileUpdate("Ana", null, null, null, null, null, null, true));

                ChatMessageResponse failed = service.close(CONVERSATION_ID);
                assertThat(profileStore.read()).isEqualTo(PatientProfile.empty());
                ChatMessageResponse retried = service.close(CONVERSATION_ID);

                assertThat(failed.status()).isEqualTo(ChatMessageResponse.Status.FAILED);
                assertThat(retried.status()).isNotEqualTo(ChatMessageResponse.Status.FAILED);
                assertThat(profileStore.read().name()).isEqualTo("Ana");
        }

    private ClinicalEventRegistrationService registeredEvents() {
        ClinicalEventRegistrationService service = mock(ClinicalEventRegistrationService.class);
        when(service.register(anyString(), any(), any(), anyString()))
                .thenReturn(new ClinicalEventRegistrationResult(
                        ClinicalEventRegistrationResult.Kind.REGISTERED, List.of(), ""));
        return service;
    }

    private MeasurementRegistrationService emptyMeasurements() {
        return new MeasurementRegistrationService(
                mock(com.careme.backend.repository.MetricMeasurementRepository.class),
                mock(MetricCatalogService.class));
    }

        private static final class FailOnceProfileStore extends PatientProfileMarkdownStore {
                private boolean fail = true;

                private FailOnceProfileStore(Path profileDirectory) {
                        super(profileDirectory);
                }

                @Override
                public void write(PatientProfile profile) throws IOException {
                        if (fail) {
                                fail = false;
                                throw new IOException("profile publication failed");
                        }
                        super.write(profile);
                }
        }
}