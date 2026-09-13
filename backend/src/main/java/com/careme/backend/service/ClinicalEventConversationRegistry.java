package com.careme.backend.service;

import com.careme.backend.entity.ClinicalEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class ClinicalEventConversationRegistry {

    private final Map<String, Map<String, ClinicalEvent>> eventsByConversation = new HashMap<>();

    public synchronized ClinicalEvent find(String conversationId, String fingerprint) {
        return eventsByConversation.getOrDefault(conversationId, Map.of()).get(fingerprint);
    }

    public synchronized void add(String conversationId, String fingerprint, ClinicalEvent event) {
        eventsByConversation
                .computeIfAbsent(conversationId, ignored -> new HashMap<>())
                .put(fingerprint, event);
    }

    public synchronized Set<String> fingerprints(String conversationId) {
        return new HashSet<>(eventsByConversation.getOrDefault(conversationId, Map.of()).keySet());
    }
}
