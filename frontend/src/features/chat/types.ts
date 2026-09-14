export type ChatStatus =
  | "registered"
  | "answered"
  | "no_records"
  | "clarification_required"
  | "general_conversation"
  | "duplicate"
  | "failed";

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
};