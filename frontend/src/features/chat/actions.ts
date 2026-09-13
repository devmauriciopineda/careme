"use server";

import { chatMessageInputSchema } from "@/features/chat/lib/schema";
import { chatService } from "@/services/chatService";

import type { ChatResponse } from "@/features/chat/types";

/**
 * Result of a chat turn, small enough to cross the server boundary.
 *
 * It names the failure instead of describing it, so the wording stays in the
 * interface and no transport detail reaches the browser.
 */
export type SendChatResult =
  | { ok: true; response: ChatResponse }
  | { ok: false; errorCode: "INVALID_INPUT" | "UPSTREAM_FAILED" };

/**
 * Sends one turn to the assistant.
 *
 * Revalidates the payload even though the interface already did, then calls the
 * chat API from the server, so the browser never needs the API address.
 */
export async function sendChatMessage(input: unknown): Promise<SendChatResult> {
  const parsed = chatMessageInputSchema.safeParse(input);

  if (!parsed.success) {
    return { ok: false, errorCode: "INVALID_INPUT" };
  }

  try {
    return { ok: true, response: await chatService.sendChatMessage(parsed.data) };
  } catch (error) {
    console.error("Failed to send the chat message:", error);
    return { ok: false, errorCode: "UPSTREAM_FAILED" };
  }
}
