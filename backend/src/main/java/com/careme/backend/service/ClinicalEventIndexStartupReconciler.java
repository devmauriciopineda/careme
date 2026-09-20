package com.careme.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ClinicalEventIndexStartupReconciler {

    private static final Logger log = LoggerFactory.getLogger(ClinicalEventIndexStartupReconciler.class);
    private final ClinicalEventIndexRebuilder rebuilder;
    private final EncounterIndexRebuilder encounterRebuilder;
    private final EncounterService encounterService;

    public ClinicalEventIndexStartupReconciler(
            ClinicalEventIndexRebuilder rebuilder,
            EncounterIndexRebuilder encounterRebuilder,
            EncounterService encounterService) {
        this.rebuilder = rebuilder;
        this.encounterRebuilder = encounterRebuilder;
        this.encounterService = encounterService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcile() {
        // The process ended with a consultation still open, which means the
        // conversation was interrupted: it is closed with what it had collected
        // before the derived indices are rebuilt.
        log.info("Closed {} consultation(s) left open", encounterService.closeAbandoned());
        try {
            log.info("Rebuilt clinical event index with {} event(s)", rebuilder.rebuild());
        } catch (Exception exception) {
            log.error("Clinical event index reconciliation failed", exception);
        }
        try {
            log.info("Rebuilt consultation index with {} consultation(s)", encounterRebuilder.rebuild());
        } catch (Exception exception) {
            log.error("Consultation index reconciliation failed", exception);
        }
    }
}