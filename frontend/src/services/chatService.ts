import { API_BASE_URL } from "./apiConfig";
import type { ChatResponse } from "@/features/chat/types";

export type ChatMessageInput = {
  message: string;
  conversationId?: string;
  messageId: string;
};

export async function sendChatMessage(input: ChatMessageInput): Promise<ChatResponse> {
  const response = await fetch(`${API_BASE_URL}/api/v1/chat/messages`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });

  const payload = (await response.json()) as ChatResponse | { message?: string };
  if (!response.ok) {
    throw new Error("message" in payload && payload.message ? payload.message : "No se pudo enviar el mensaje.");
  }
  return payload as ChatResponse;
}