import { afterEach, describe, expect, it, vi } from "vitest";

import { chatService } from "@/services/chatService";
import { sendChatMessage } from "./actions";

vi.mock("@/services/chatService");

const chatServiceMock = vi.mocked(chatService);

const RESPONSE = {
  conversationId: "conversation-1",
  messageId: "message-1",
  status: "registered" as const,
  message: "Hecho registrado en tu historia clínica.",
  events: [],
};

function silenceConsoleError() {
  return vi.spyOn(console, "error").mockImplementation(() => {});
}

afterEach(() => {
  chatServiceMock.sendChatMessage.mockReset();
  vi.restoreAllMocks();
});

describe("sendChatMessage", () => {
  it("sends a validated turn and returns the response", async () => {
    chatServiceMock.sendChatMessage.mockResolvedValue(RESPONSE);

    const result = await sendChatMessage({ message: "Ayer tuve fiebre", messageId: "message-1" });

    expect(result).toEqual({ ok: true, response: RESPONSE });
    expect(chatServiceMock.sendChatMessage).toHaveBeenCalledWith({
      message: "Ayer tuve fiebre",
      messageId: "message-1",
    });
  });

  it("refuses an invalid payload before the API is called", async () => {
    const result = await sendChatMessage({ message: "   ", messageId: "message-1" });

    expect(result).toEqual({ ok: false, errorCode: "INVALID_INPUT" });
    expect(chatServiceMock.sendChatMessage).not.toHaveBeenCalled();
  });

  it("refuses a blank message identifier before the API is called", async () => {
    const result = await sendChatMessage({ message: "Ayer tuve fiebre", messageId: "" });

    expect(result).toEqual({ ok: false, errorCode: "INVALID_INPUT" });
    expect(chatServiceMock.sendChatMessage).not.toHaveBeenCalled();
  });

  it("names an upstream failure without carrying its detail", async () => {
    silenceConsoleError();
    chatServiceMock.sendChatMessage.mockRejectedValue(new Error("Failed to fetch"));

    const result = await sendChatMessage({ message: "Ayer tuve fiebre", messageId: "message-1" });

    expect(result).toEqual({ ok: false, errorCode: "UPSTREAM_FAILED" });
  });
});
