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

const chatEventSchema = z.object({
  code: z.string(),
  type: z.string(),
  date: z.string().nullable(),
  datePrecision: z.string(),
  content: z.string(),
});

/** One turn as the backend returns it. */
export const chatResponseSchema = z.object({
  conversationId: z.string(),
  messageId: z.string(),
  status: z.enum([
    "registered",
    "answered",
    "no_records",
    "clarification_required",
    "general_conversation",
    "duplicate",
    "failed",
  ]),
  message: z.string(),
  events: z.array(chatEventSchema),
});
