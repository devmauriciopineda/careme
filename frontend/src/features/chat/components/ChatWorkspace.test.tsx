import { render, screen, within } from "@testing-library/react";
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
    ["answered", "Respuesta"],
    ["no_records", "Sin registros"],
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

  it("shows an answer together with the events that support it and their precision", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "answered",
        message: "Te la diagnosticaron en enero.",
        events: [
          {
            code: "evt_001",
            type: "diagnosis",
            date: "2026-01-10",
            datePrecision: "approximate",
            content: "Hipertensión diagnosticada",
          },
        ],
      },
    });

    render(<ChatWorkspace />);
    await send(user, "¿Cuándo me diagnosticaron hipertensión?");

    expect(await screen.findByText("Te la diagnosticaron en enero.")).toBeInTheDocument();
    const supporting = screen.getByRole("list", { name: "Hechos que sustentan la respuesta" });
    expect(within(supporting).getByText("evt_001")).toBeInTheDocument();
    expect(within(supporting).getByText(/fecha aproximada/)).toBeInTheDocument();
    expect(within(supporting).getByText("Hipertensión diagnosticada")).toBeInTheDocument();
  });

  it("announces a no-records turn without leaking technical detail", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(
      succeeds("no_records", "No encontré registros en tu historia clínica.")
    );

    render(<ChatWorkspace />);
    await send(user, "¿He tenido migrañas?");

    expect(await screen.findByText("No encontré registros en tu historia clínica.")).toBeInTheDocument();
    expect(screen.getByText("Sin registros")).toBeInTheDocument();
    expect(screen.queryByLabelText("Hechos que sustentan la respuesta")).not.toBeInTheDocument();
    expect(screen.queryByText(/prompt|stack trace|credential/i)).not.toBeInTheDocument();
  });

  it("shows why no records were found and what the user can do next", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "no_records",
        message: "No encuentro registros en ese periodo de tu historia clínica.",
        events: [],
        absenceReason: "no_events_in_period",
        suggestedActions: ["reformulate", "register"],
      },
    });

    render(<ChatWorkspace />);
    await send(user, "¿Qué me pasó el año pasado?");

    expect(await screen.findByText("Sin registros")).toBeInTheDocument();
    expect(screen.getByText("Sin registros en ese periodo")).toBeInTheDocument();
    const actions = screen.getByRole("list", { name: "Qué puedes hacer ahora" });
    expect(within(actions).getByText("Reformular la pregunta con otras palabras")).toBeInTheDocument();
    expect(within(actions).getByText("Contar el hecho para registrarlo")).toBeInTheDocument();
  });

  it("keeps a no-records turn apart from an answer, an error and any technical detail", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "no_records",
        message: "Todavía no hay hechos registrados en tu historia clínica. Que no encuentre registros no significa que no haya ocurrido: solo que no consta en tu historia clínica.",
        events: [],
        absenceReason: "empty_history",
        suggestedActions: ["reformulate", "register"],
      },
    });

    render(<ChatWorkspace />);
    await send(user, "¿He tenido migrañas?");

    expect(await screen.findByText("Sin registros")).toBeInTheDocument();
    expect(screen.getByText("Todavía no hay hechos registrados en tu historia")).toBeInTheDocument();
    expect(screen.queryByLabelText("Hechos que sustentan la respuesta")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Reintentar" })).not.toBeInTheDocument();
    expect(screen.queryByText("No se pudo completar")).not.toBeInTheDocument();
    expect(screen.queryByText(/prompt|stack trace|credential|deepseek/i)).not.toBeInTheDocument();
  });

  it("shows a general conversation turn as conversation, without events or absence", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(
      succeeds("general_conversation", "Es una condición que se mide y se valora en consulta.")
    );

    render(<ChatWorkspace />);
    await send(user, "¿Qué es la hipertensión?");

    expect(await screen.findByText("Conversación")).toBeInTheDocument();
    expect(screen.getByText("Es una condición que se mide y se valora en consulta.")).toBeInTheDocument();
    expect(screen.queryByRole("list", { name: "Hechos que sustentan la respuesta" })).not.toBeInTheDocument();
    expect(screen.queryByText("Sin registros")).not.toBeInTheDocument();
    expect(screen.queryByText("No se pudo completar")).not.toBeInTheDocument();
  });

  it("shows a declination as conversation and adds no clinical advice of its own", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue(
      succeeds(
        "general_conversation",
        "No puedo darte un diagnóstico ni recomendarte un tratamiento: este asistente registra y consulta tu historia clínica, pero no diagnostica ni recomienda."
      )
    );

    render(<ChatWorkspace />);
    await send(user, "¿Qué me recomiendas para la hipertensión?");

    expect(await screen.findByText(/No puedo darte un diagnóstico/)).toBeInTheDocument();
    expect(screen.getByText("Conversación")).toBeInTheDocument();
    expect(screen.queryByText("Respuesta")).not.toBeInTheDocument();
    expect(screen.queryByRole("list", { name: "Hechos que sustentan la respuesta" })).not.toBeInTheDocument();
    expect(screen.queryByText(/te recomiendo|deberías tomar/i)).not.toBeInTheDocument();
  });

  it("shows the general part of a mixed turn apart from the grounded answer", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "answered",
        message: "Te la diagnosticaron en enero.",
        events: [
          {
            code: "evt_001",
            type: "diagnosis",
            date: "2026-01-10",
            datePrecision: "exact",
            content: "Hipertensión diagnosticada",
          },
        ],
        generalReply: "Es una condición que se mide y se valora en consulta.",
      },
    });

    render(<ChatWorkspace />);
    await send(user, "¿Qué es la hipertensión? ¿Cuándo me la diagnosticaron?");

    expect(await screen.findByText("Te la diagnosticaron en enero.")).toBeInTheDocument();

    const conversation = screen.getByRole("group", { name: "Conversación general" });
    expect(within(conversation).getByText("Es una condición que se mide y se valora en consulta.")).toBeInTheDocument();
    expect(within(conversation).queryByText("Hipertensión diagnosticada")).not.toBeInTheDocument();

    const supporting = screen.getByRole("list", { name: "Hechos que sustentan la respuesta" });
    expect(within(supporting).getByText("Hipertensión diagnosticada")).toBeInTheDocument();
    expect(
      within(supporting).queryByText("Es una condición que se mide y se valora en consulta.")
    ).not.toBeInTheDocument();
  });

  it("names the conversational block and leaks no internal detail", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "no_records",
        message: "No encuentro registros que respondan a tu pregunta.",
        events: [],
        absenceReason: "no_term_match",
        suggestedActions: ["reformulate", "register"],
        generalReply: "Es una condición que se mide y se valora en consulta.",
      },
    });

    render(<ChatWorkspace />);
    await send(user, "¿Qué es la hipertensión? ¿He tenido migrañas?");

    const conversation = await screen.findByRole("group", { name: "Conversación general" });
    expect(within(conversation).getByText("Es una condición que se mide y se valora en consulta.")).toBeInTheDocument();
    expect(screen.getByText("Sin registros")).toBeInTheDocument();
    expect(screen.queryByText(/prompt|stack trace|credential|deepseek/i)).not.toBeInTheDocument();
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