export type EventType = "diagnosis" | "medication" | "measurement" | "note";
export type EventSort = "recordDate" | "occurrenceDate";

export type ClinicalEvent = {
    id: string;
    code: string;
    type: EventType;
    content: string;
    occurrenceDate: string | null;
    occurrenceDatePrecision: "exact" | "approximate" | "unknown";
    recordDate: string;
};