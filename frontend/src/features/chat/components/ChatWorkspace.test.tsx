import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { ChatWorkspace } from "./ChatWorkspace";

describe("ChatWorkspace", () => {
  it("sends a message and renders the assistant response", async () => {
    const user = userEvent.setup();
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "registered",
        message: "Hecho registrado en tu historia clínica.",
        events: [],
      }),
    }));

    render(<ChatWorkspace />);
    await user.type(screen.getByLabelText("Mensaje para el asistente"), "Ayer tuve fiebre");
    await user.click(screen.getByRole("button", { name: "Enviar mensaje" }));

    expect(await screen.findByText("Hecho registrado en tu historia clínica.")).toBeInTheDocument();
    expect(fetch).toHaveBeenCalledWith(
      expect.stringContaining("/api/v1/chat/messages"),
      expect.objectContaining({ method: "POST" }),
    );
  });

  it.each([
    ["clarification_required", "Necesita aclaración"],
    ["general_conversation", "Conversación"],
    ["duplicate", "Ya estaba registrado"],
    ["failed", "No se pudo completar"],
  ] as const)("renders %s outcome", async (status, label) => {
    const user = userEvent.setup();
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        conversationId: "conversation-1",
        messageId: "message-1",
        status,
        message: "Respuesta del asistente",
      }),
    }));

    render(<ChatWorkspace />);
    await user.type(screen.getByLabelText("Mensaje para el asistente"), "Tuve fiebre");
    await user.click(screen.getByRole("button", { name: "Enviar mensaje" }));

    expect(await screen.findByText("Respuesta del asistente")).toBeInTheDocument();
    expect(screen.getByText(label)).toBeInTheDocument();
  });

  it("preserves failed input and retries only after explicit action", async () => {
    const user = userEvent.setup();
    const fetchMock = vi.fn()
      .mockRejectedValueOnce(new Error("No se pudo enviar el mensaje."))
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          conversationId: "conversation-1",
          messageId: "message-2",
          status: "registered",
          message: "Hecho registrado en tu historia clínica.",
        }),
      });
    vi.stubGlobal("fetch", fetchMock);

    render(<ChatWorkspace />);
    await user.type(screen.getByLabelText("Mensaje para el asistente"), "Tuve fiebre");
    await user.click(screen.getByRole("button", { name: "Enviar mensaje" }));
    await user.click(await screen.findByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("Hecho registrado en tu historia clínica.")).toBeInTheDocument();
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls[1][0]).toContain("/api/v1/chat/messages");
  });
});