import { chatResponseSchema } from "@/features/chat/lib/schema";
import { API_BASE_URL } from "./apiConfig";

import type { ChatMessageInput } from "@/features/chat/lib/schema";
import type { ChatResponse } from "@/features/chat/types";

const CHAT_PATH = "/api/v1/chat/messages";

/** The close operation ends the consultation of one conversation. */
function consultationClosePath(conversationId: string): string {
  return `/api/v1/chat/conversations/${encodeURIComponent(conversationId)}/consultation/close`;
}

/**
 * The API could not be reached, or answered something this app cannot read.
 *
 * It carries no transport detail: the caller words the failure for the user.
 */
export class ChatUnavailableError extends Error {
  constructor(cause?: unknown) {
    super("The chat API is unavailable");
    this.name = "ChatUnavailableError";
    this.cause = cause;
  }
}

/**
 * Data access boundary for the assistant chat.
 *
 * Server-only: it reads `API_BASE_URL`, which is deliberately never exposed to
 * the browser. It calls the chat API and validates the response before returning
 * it, so the rest of the app only ever sees a well-formed turn.
 */
export const chatService = {
  async sendChatMessage(input: ChatMessageInput): Promise<ChatResponse> {
    let response: Response;

    try {
      response = await fetch(`${API_BASE_URL}${CHAT_PATH}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(input),
        cache: "no-store",
      });
    } catch (error) {
      console.error("Failed to reach the chat API:", error);
      throw new ChatUnavailableError(error);
    }

    if (!response.ok) {
      console.error(`Chat API responded with ${response.status}`);
      throw new ChatUnavailableError();
    }

    try {
      return chatResponseSchema.parse(await response.json());
    } catch (error) {
      console.error("Chat API response does not match the contract:", error);
      throw new ChatUnavailableError(error);
    }
  },

  /**
   * Ends the consultation in progress and returns the close outcome.
   *
   * The close is idempotent on the backend, so a retry never registers the same
   * facts twice.
   */
  async closeConsultation(conversationId: string): Promise<ChatResponse> {
    let response: Response;

    try {
      response = await fetch(`${API_BASE_URL}${consultationClosePath(conversationId)}`, {
        method: "POST",
        cache: "no-store",
      });
    } catch (error) {
      console.error("Failed to reach the chat API:", error);
      throw new ChatUnavailableError(error);
    }

    if (!response.ok) {
      console.error(`Chat API responded with ${response.status}`);
      throw new ChatUnavailableError();
    }

    try {
      return chatResponseSchema.parse(await response.json());
    } catch (error) {
      console.error("Chat API response does not match the contract:", error);
      throw new ChatUnavailableError(error);
    }
  },
};