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

    public ClinicalEventIndexStartupReconciler(ClinicalEventIndexRebuilder rebuilder) {
        this.rebuilder = rebuilder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void reconcile() {
        try {
            log.info("Rebuilt clinical event index with {} event(s)", rebuilder.rebuild());
        } catch (Exception exception) {
            log.error("Clinical event index reconciliation failed", exception);
        }
    }
}