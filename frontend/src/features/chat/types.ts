export type ChatStatus =
  | "registered"
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
  | "no_term_match";

/** What the user is offered after an absence, so the turn stays actionable. */
export type SuggestedAction = "reformulate" | "register";

export type ChatResponse = {
  conversationId: string;
  messageId: string;
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
  }>;
};