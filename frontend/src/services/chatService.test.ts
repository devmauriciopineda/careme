import { afterEach, describe, expect, it, vi } from "vitest";

import { ChatUnavailableError, chatService } from "./chatService";

const CHAT_RESPONSE = {
    conversationId: "conversation-1",
    messageId: "message-1",
    status: "registered",
    message: "Hecho registrado en tu historia clínica.",
    events: [
        {
            code: "evt_001",
            type: "diagnosis",
            date: "2026-09-12",
            datePrecision: "exact",
            content: "Ayer me diagnosticaron hipertensión",
        },
    ],
};

const INPUT = { message: "Ayer tuve fiebre", messageId: "message-1" };

function stubFetch(response: Partial<Response>) {
    const fetchMock = vi.fn().mockResolvedValue(response as Response);
    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

function stubFailingFetch(error: unknown) {
    const fetchMock = vi.fn().mockRejectedValue(error);
    vi.stubGlobal("fetch", fetchMock);
    return fetchMock;
}

function silenceConsoleError() {
    return vi.spyOn(console, "error").mockImplementation(() => {});
}

afterEach(() => {
    vi.unstubAllGlobals();
    vi.restoreAllMocks();
});

describe("chatService.sendChatMessage", () => {
    it("posts the turn and returns the validated response", async () => {
        const fetchMock = stubFetch({
            ok: true,
            status: 200,
            json: async () => CHAT_RESPONSE,
        });

        const response = await chatService.sendChatMessage(INPUT);

        expect(fetchMock).toHaveBeenCalledWith(
            expect.stringContaining("/api/v1/chat/messages"),
            expect.objectContaining({ method: "POST", cache: "no-store" })
        );
        expect(response).toEqual(CHAT_RESPONSE);
    });

    it("rejects a response that does not match the contract", async () => {
        silenceConsoleError();
        stubFetch({ ok: true, status: 200, json: async () => ({ conversationId: "only-this" }) });

        await expect(chatService.sendChatMessage(INPUT)).rejects.toBeInstanceOf(ChatUnavailableError);
    });

    it("collapses a transport failure into one error", async () => {
        silenceConsoleError();
        stubFailingFetch(new TypeError("Failed to fetch"));

        await expect(chatService.sendChatMessage(INPUT)).rejects.toBeInstanceOf(ChatUnavailableError);
    });

    it("collapses a non-2xx status into one error", async () => {
        silenceConsoleError();
        stubFetch({ ok: false, status: 500, json: async () => ({}) });

        await expect(chatService.sendChatMessage(INPUT)).rejects.toBeInstanceOf(ChatUnavailableError);
    });

    it("carries no transport detail in the error it raises", async () => {
        silenceConsoleError();
        stubFailingFetch(new TypeError("Failed to fetch"));

        await expect(chatService.sendChatMessage(INPUT)).rejects.toThrow("The chat API is unavailable");
    });
});
