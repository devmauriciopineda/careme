export type ChatStatus =
  | "noted"
  | "registered"
  | "nothing_to_register"
  | "answered"
  | "no_records"
  | "clarification_required"
  | "general_conversation"
  | "duplicate"
  | "failed";

/**
 * Why the history could not answer a question, as the backend reports it.
 *
 * It is a value and not prose, so the interface picks the wording and never has
 * to read the Spanish message to tell one absence from another.
 */
export type AbsenceReason =
  | "empty_history"
  | "no_events_of_type"
  | "no_events_in_period"
  | "no_term_match"
  | "no_measurements"
  | "no_measurements_in_period"
  | "metric_not_tracked";

/** What the user is offered after an absence, so the turn stays actionable. */
export type SuggestedAction = "reformulate" | "register";

/** One component of a measurement, written as the tracking stores it. */
export type MeasurementValue = {
  component: string;
  value: string;
};

/**
 * One measurement that supports an answer: its metric, its values in the reference
 * unit of that metric and its exact date. A composite metric is one measurement
 * with several values, so it is never presented as separate measurements.
 */
export type MeasurementSummary = {
  reference: string;
  metric: string;
  label: string;
  unit: string;
  date: string;
  values: MeasurementValue[];
  /** The whole measurement written out by the backend, with its unit explicit. */
  text: string;
};

export type ChatResponse = {
  conversationId: string;
  messageId: string | null;
  status: ChatStatus;
  message: string;
  events: Array<{
    code: string;
    type: string;
    date: string | null;
    datePrecision: string;
    content: string;
  }>;
  /** Present on a `no_records` turn; absent or null otherwise. */
  absenceReason?: AbsenceReason | null;
  /** Present on a `no_records` turn; absent or empty otherwise. */
  suggestedActions?: SuggestedAction[];
  /**
   * Every operation the turn went through, in the order it ran. `message` is the
   * single answer; this lets the interface tell what was completed from what was
   * not, instead of presenting an incomplete turn as fully successful.
   */
  operations?: Array<{
    status: ChatStatus;
    events: ChatResponse["events"];
    absenceReason?: AbsenceReason | null;
    suggestedActions?: SuggestedAction[];
    measurements?: MeasurementSummary[];
  }>;
  /**
   * The measurements the answer is grounded in, when the turn answered from the
   * tracking. They travel apart from the clinical events: a measurement is a
   * different record, with its own unit and its own exact date.
   */
  measurements?: MeasurementSummary[];
};