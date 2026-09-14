package com.careme.backend.service;

import com.careme.backend.dto.ClinicalEventIntent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public interface ClinicalIntentInterpreter {
    ClinicalEventIntent interpret(String message, InterpretationContext context);

    record InterpretationContext(LocalDate referenceDate, ZoneId zoneId, String promptVersion, List<String> recentTurns) {

        public InterpretationContext {
            recentTurns = recentTurns == null ? List.of() : List.copyOf(recentTurns);
        }

        /** Context for a turn with no recent turns to lean on. */
        public InterpretationContext(LocalDate referenceDate, ZoneId zoneId, String promptVersion) {
            this(referenceDate, zoneId, promptVersion, List.of());
        }
    }
}