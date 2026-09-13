package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import java.time.LocalDate;
import java.time.ZoneId;

public interface ClinicalIntentInterpreter {
    ClinicalEventIntent interpret(String message, InterpretationContext context);

    record InterpretationContext(LocalDate referenceDate, ZoneId zoneId, String promptVersion) {
    }
}