import { z } from "zod";

/**
 * Validates chat payloads at the data boundary, so a malformed or changed API
 * response fails here instead of deep inside the interface.
 */

/** Longest message the backend accepts. */
const MESSAGE_MAX_LENGTH = 4000;

/**
 * The payload the server action accepts. It mirrors the backend rules, so an
 * invalid turn never leaves the server.
 */
export const chatMessageInputSchema = z.object({
  message: z.string().trim().min(1).max(MESSAGE_MAX_LENGTH),
  conversationId: z.string().min(1).optional(),
  messageId: z.string().min(1),
});

/** The payload of one turn, as validated by `chatMessageInputSchema`. */
export type ChatMessageInput = z.infer<typeof chatMessageInputSchema>;

/**
 * The conversation whose consultation is being closed. It mirrors the backend
 * path variable, so a blank identifier never leaves the server.
 */
export const conversationIdSchema = z.string().trim().min(1);

const chatEventSchema = z.object({
  code: z.string(),
  type: z.string(),
  date: z.string().nullable(),
  datePrecision: z.string(),
  content: z.string(),
});

/**
 * Why the history could not answer. Only these values are accepted: an unknown
 * reason would leave the interface unable to say what the absence means.
 */
const absenceReasonSchema = z.enum([
  "empty_history",
  "no_events_of_type",
  "no_events_in_period",
  "no_term_match",
]);

/** What the user is offered after an absence. */
const suggestedActionSchema = z.enum(["reformulate", "register"]);

/** The outcome of one turn. */
const chatStatusSchema = z.enum([
  "noted",
  "registered",
  "nothing_to_register",
  "answered",
  "no_records",
  "clarification_required",
  "general_conversation",
  "duplicate",
  "failed",
]);

/**
 * One operation the turn went through, with its own result. The turn's message is
 * the single answer; this is what lets the interface tell what was completed from
 * what was not.
 */
const operationSummarySchema = z.object({
  status: chatStatusSchema,
  events: z.array(chatEventSchema),
  absenceReason: absenceReasonSchema.nullish(),
  suggestedActions: z.array(suggestedActionSchema).optional(),
});

/** One turn as the backend returns it. */
export const chatResponseSchema = z.object({
  conversationId: z.string(),
  messageId: z.string().nullable(),
  status: chatStatusSchema,
  message: z.string(),
  events: z.array(chatEventSchema),
  absenceReason: absenceReasonSchema.nullish(),
  suggestedActions: z.array(suggestedActionSchema).optional(),
  operations: z.array(operationSummarySchema).optional(),
});
