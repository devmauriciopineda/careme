package com.careme.backend.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.entity.PatientProfile;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatMessageResponseTest {

    private static final ProfileSummary PROFILE = ProfileSummary.of(
            new PatientProfile("Ana", null, null, null, null, null, null));

    @Test
    void carriesTheProfileWithoutLosingTheRestOfTheOutcome() {
        ChatMessageResponse response = new ChatMessageResponse(
                "conversation-1", "message-1", ChatMessageResponse.Status.NOTED, "Lo he anotado.", List.of());

        ChatMessageResponse withProfile = response.withProfile(PROFILE);

        assertThat(withProfile.profile()).isEqualTo(PROFILE);
        assertThat(withProfile.conversationId()).isEqualTo("conversation-1");
        assertThat(withProfile.messageId()).isEqualTo("message-1");
        assertThat(withProfile.status()).isEqualTo(ChatMessageResponse.Status.NOTED);
        assertThat(withProfile.message()).isEqualTo("Lo he anotado.");
        assertThat(withProfile.events()).isEqualTo(response.events());
        assertThat(withProfile.operations()).isEqualTo(response.operations());
        assertThat(withProfile.measurements()).isEqualTo(response.measurements());
        assertThat(response.profile()).isNull();
    }

    @Test
    void keepsTheProfileWhenAnotherProjectionIsApplied() {
        ChatMessageResponse response = new ChatMessageResponse(
                        "conversation-1", "message-1", ChatMessageResponse.Status.NOTED, "Lo he anotado.", List.of())
                .withProfile(PROFILE);

        ChatMessageResponse withOperations = response.withOperations(List.of());
        ChatMessageResponse withMeasurements = response.withMeasurements(List.of());

        assertThat(withOperations.profile()).isEqualTo(PROFILE);
        assertThat(withMeasurements.profile()).isEqualTo(PROFILE);
    }

    @Test
    void normalizesAnOperationSummaryWithNoCollectionsAndCarriesItsMeasurements() {
        ChatMessageResponse.OperationSummary empty = ChatMessageResponse.OperationSummary.of(
                ChatMessageResponse.Status.NOTED, null, null, null);

        assertThat(empty.events()).isEmpty();
        assertThat(empty.suggestedActions()).isEmpty();
        assertThat(empty.measurements()).isEmpty();

        ChatMessageResponse.OperationSummary withMeasurements = empty.withMeasurements(List.of(
                new ChatMessageResponse.MeasurementSummary(
                        "weight@2026-01-10", "weight", "peso", "kg", "2026-01-10",
                        List.of(new ChatMessageResponse.MeasurementSummary.ValueSummary("value", "70.5")),
                        "peso: 70.5 kg")));

        assertThat(withMeasurements.measurements()).hasSize(1);
        assertThat(withMeasurements.status()).isEqualTo(ChatMessageResponse.Status.NOTED);

        ChatMessageResponse.OperationSummary normalized = new ChatMessageResponse.OperationSummary(
                ChatMessageResponse.Status.NOTED, null, null, null, null);
        assertThat(normalized.events()).isEmpty();
        assertThat(normalized.suggestedActions()).isEmpty();
        assertThat(normalized.measurements()).isEmpty();
    }

    @Test
    void normalizesAMeasurementSummaryWithNoValues() {
        ChatMessageResponse.MeasurementSummary summary = new ChatMessageResponse.MeasurementSummary(
                "weight@2026-01-10", "weight", "peso", "kg", "2026-01-10", null, "peso: 70.5 kg");

        assertThat(summary.values()).isEmpty();
    }
}
