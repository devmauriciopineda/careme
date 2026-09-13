package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import com.careme.backend.entity.ClinicalEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "careme.llm.mode", havingValue = "fake", matchIfMissing = true)
public class FakeClinicalIntentInterpreter implements ClinicalIntentInterpreter {

    @Override
    public ClinicalEventIntent interpret(String message, InterpretationContext context) {
        String normalized = message.trim().toLowerCase();
        if (normalized.contains("creo que") || normalized.contains("podría tener")
                || normalized.contains("podria tener")) {
            return ClinicalEventIntent.conversation();
        }
        if (normalized.contains("aclara") || normalized.length() < 12) {
            return ClinicalEventIntent.clarification("¿Qué hecho médico quieres registrar y cuándo ocurrió?");
        }
        if (!containsClinicalSignal(normalized)) {
            return ClinicalEventIntent.conversation();
        }

        ClinicalEvent.ClinicalEventType type = normalized.contains("medic")
                ? ClinicalEvent.ClinicalEventType.MEDICATION
                : normalized.contains("presion") || normalized.contains("presión")
                        ? ClinicalEvent.ClinicalEventType.MEASUREMENT
                        : normalized.contains("diagnostic")
                                ? ClinicalEvent.ClinicalEventType.DIAGNOSIS
                                : ClinicalEvent.ClinicalEventType.NOTE;
        LocalDate date = normalized.contains("hoy") ? context.referenceDate()
                : normalized.contains("ayer") ? context.referenceDate().minusDays(1) : null;
        ClinicalEvent.DatePrecision precision = date == null
                ? ClinicalEvent.DatePrecision.UNKNOWN
                : ClinicalEvent.DatePrecision.EXACT;
        String dateText = normalized.contains("hoy") ? "hoy" : normalized.contains("ayer") ? "ayer" : null;
        return new ClinicalEventIntent(
                ClinicalEventIntent.Kind.EVENTS,
                List.of(new ClinicalEventIntent.Candidate(type, message.trim(), date, precision, dateText)),
                null);
    }

    private boolean containsClinicalSignal(String message) {
        return message.contains("diagnostic") || message.contains("medic") || message.contains("dolor")
                || message.contains("fiebre") || message.contains("presion") || message.contains("presión")
                || message.contains("hospital") || message.contains("operación") || message.contains("operacion");
    }
}