import {
    clinicalEventResponseSchema,
    clinicalEventsResponseSchema,
} from "@/features/clinical-events/lib/schema";
import type { ClinicalEvent, EventSort, EventType } from "@/features/clinical-events/types";
import { API_BASE_URL } from "@/services/apiConfig";

const EVENTS_PATH = "/api/v1/clinical-events";

export const clinicalEventService = {
    async getEvents(options: {
        type?: EventType;
        from?: string;
        to?: string;
        sort?: EventSort;
    } = {}): Promise<ClinicalEvent[]> {
        const query = new URLSearchParams();
        if (options.type) query.set("type", options.type);
        if (options.from) query.set("from", options.from);
        if (options.to) query.set("to", options.to);
        if (options.sort) query.set("sort", options.sort);

        const suffix = query.size > 0 ? `?${query.toString()}` : "";
        const response = await fetch(`${API_BASE_URL}${EVENTS_PATH}${suffix}`, {
            cache: "no-store",
        });
        if (!response.ok) {
            throw new Error(`Clinical events API responded with ${response.status}`);
        }
        return clinicalEventsResponseSchema.parse(await response.json()).data;
    },

    async getEvent(code: string): Promise<ClinicalEvent | null> {
        const response = await fetch(`${API_BASE_URL}${EVENTS_PATH}/${code}`, {
            cache: "no-store",
        });
        if (response.status === 404) return null;
        if (!response.ok) {
            throw new Error(`Clinical event API responded with ${response.status}`);
        }
        return clinicalEventResponseSchema.parse(await response.json()).data;
    },
};