import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { sendChatMessage } from "@/features/chat/actions";
import { ChatWorkspace } from "./ChatWorkspace";

import type { SendChatResult } from "@/features/chat/actions";
import type { ChatMessageInput } from "@/features/chat/lib/schema";
import type { ChatStatus } from "@/features/chat/types";

vi.mock("@/features/chat/actions");

const sendChatMessageMock = vi.mocked(sendChatMessage);

/** A turn the backend completed, carrying the given status. */
function succeeds(status: ChatStatus, message = "Respuesta del asistente"): SendChatResult {
  return {
    ok: true,
    response: { conversationId: "conversation-1", messageId: "message-1", status, message, events: [] },
  };
}

/** A turn the boundary refused, without naming a transport cause. */
const REFUSED: SendChatResult = { ok: false, errorCode: "UPSTREAM_FAILED" };

/** The payloads the interface has handed to the action, oldest first. */
function sentPayloads(): ChatMessageInput[] {
  return sendChatMessageMock.mock.calls.map(([input]) => input as ChatMessageInput);
}

async function send(user: ReturnType<typeof userEvent.setup>, text: string) {
  await user.type(screen.getByLabelText("Mensaje para el asistente"), text);
  await user.click(screen.getByRole("button", { name: "Enviar mensaje" }));
}

beforeEach(() => {
  sendChatMessageMock.mockReset();
});

describe("ChatWorkspace", () => {
  it("sends a message and renders the assistant response", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(succeeds("registered", "Hecho registrado en tu historia clínica."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");

    expect(await screen.findByText("Hecho registrado en tu historia clínica.")).toBeInTheDocument();
    expect(sendChatMessageMock).toHaveBeenCalledWith({
      message: "Ayer tuve fiebre",
      conversationId: undefined,
      messageId: expect.any(String),
    });
  });

  it.each([
    ["clarification_required", "Necesita aclaración"],
    ["general_conversation", "Conversación"],
    ["duplicate", "Ya estaba registrado"],
    ["failed", "No se pudo completar"],
  ] as const)("renders %s outcome", async (status, label) => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(succeeds(status));

    render(<ChatWorkspace />);
    await send(user, "Tuve fiebre");

    expect(await screen.findByText("Respuesta del asistente")).toBeInTheDocument();
    expect(screen.getByText(label)).toBeInTheDocument();
  });

  it("preserves failed input and retries only after explicit action", async () => {
    const user = userEvent.setup();
    sendChatMessageMock
      .mockResolvedValueOnce(REFUSED)
      .mockResolvedValueOnce(succeeds("registered", "Hecho registrado en tu historia clínica."));

    render(<ChatWorkspace />);
    await send(user, "Tuve fiebre");
    await user.click(await screen.findByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("Hecho registrado en tu historia clínica.")).toBeInTheDocument();
    expect(sendChatMessageMock).toHaveBeenCalledTimes(2);

    const [first, second] = sentPayloads();
    expect(second.message).toBe("Tuve fiebre");
    expect(second.messageId).not.toBe(first.messageId);
  });

  it("words the failure without leaking transport detail", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(REFUSED);

    render(<ChatWorkspace />);
    await send(user, "Tuve fiebre");

    expect(await screen.findByText("No se pudo completar el envío. Inténtalo de nuevo.")).toBeInTheDocument();
    expect(screen.queryByText(/Failed to fetch/)).not.toBeInTheDocument();
  });

  it("prevents a second turn while one is pending and exposes a busy state", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockReturnValue(new Promise<SendChatResult>(() => {}));

    render(<ChatWorkspace />);
    await send(user, "Tuve fiebre");

    expect(await screen.findByRole("status")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Enviar mensaje" }));

    expect(sendChatMessageMock).toHaveBeenCalledTimes(1);
  });
});