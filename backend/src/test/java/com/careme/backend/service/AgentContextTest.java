package com.careme.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careme.backend.entity.PatientProfile;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class AgentContextTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 9, 30);

    @Test
    void defaultsToAnEmptyProfileWhenNoneIsGiven() {
        AgentContext context = new AgentContext(
                "conversation-1", REFERENCE_DATE, ZoneId.of("Europe/Madrid"), List.of());

        assertThat(context.profile()).isEqualTo(PatientProfile.empty());
    }

    @Test
    void keepsTheProfileItWasGiven() {
        PatientProfile profile = new PatientProfile("Ana", null, null, null, null, null, null);
        AgentContext context = new AgentContext(
                "conversation-1", REFERENCE_DATE, ZoneId.of("Europe/Madrid"), List.of(), profile);

        assertThat(context.profile()).isEqualTo(profile);
    }

    @Test
    void treatsAnAbsentProfileAndAnAbsentTurnListAsEmptyOnes() {
        AgentContext context = new AgentContext("conversation-1", REFERENCE_DATE, ZoneId.of("Europe/Madrid"), null, null);

        assertThat(context.profile()).isEqualTo(PatientProfile.empty());
        assertThat(context.recentTurns()).isEmpty();
    }
}
