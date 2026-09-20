import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";

import { closeConsultation, sendChatMessage } from "@/features/chat/actions";
import { ChatWorkspace } from "./ChatWorkspace";

import type { CloseConsultationResult, SendChatResult } from "@/features/chat/actions";
import type { ChatMessageInput } from "@/features/chat/lib/schema";
import type { ChatResponse, ChatStatus } from "@/features/chat/types";

vi.mock("@/features/chat/actions");

const sendChatMessageMock = vi.mocked(sendChatMessage);
const closeConsultationMock = vi.mocked(closeConsultation);

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
  closeConsultationMock.mockReset();
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

  it("shows a turn that registered a fact and answered a question as one answer", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "answered",
        message: "He registrado el diagnóstico y esto es lo que consta.",
        events: [
          {
            code: "evt_001",
            type: "diagnosis",
            date: "2026-01-10",
            datePrecision: "exact",
            content: "Hipertensión diagnosticada",
          },
        ],
        operations: [
          { status: "registered", events: [] },
          {
            status: "answered",
            events: [
              {
                code: "evt_001",
                type: "diagnosis",
                date: "2026-01-10",
                datePrecision: "exact",
                content: "Hipertensión diagnosticada",
              },
            ],
          },
        ],
      },
    });

    render(<ChatWorkspace />);
    await send(user, "Me diagnosticaron hipertensión. ¿Qué diagnósticos tengo?");

    expect(await screen.findByText("He registrado el diagnóstico y esto es lo que consta.")).toBeInTheDocument();

    const operations = screen.getByRole("list", { name: "Operaciones del turno" });
    expect(within(operations).getByText("Registrado")).toBeInTheDocument();
    expect(within(operations).getByText("Respuesta")).toBeInTheDocument();

    const supporting = screen.getByRole("list", { name: "Hechos que sustentan la respuesta" });
    expect(within(supporting).getByText("Hipertensión diagnosticada")).toBeInTheDocument();
  });

  it("shows an operation that did not complete apart from the ones that did", async () => {
    const user = userEvent.setup();
    sendChatMessageMock.mockResolvedValue({
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: "message-1",
        status: "failed",
        message: "No he podido completar lo que me pedías. Puedes reintentarlo.",
        events: [],
        operations: [
          { status: "registered", events: [] },
          { status: "failed", events: [] },
        ],
      },
    });

    render(<ChatWorkspace />);
    await send(user, "Registra esto y dime qué consta.");

    expect(await screen.findByText(/No he podido completar/)).toBeInTheDocument();

    const operations = screen.getByRole("list", { name: "Operaciones del turno" });
    expect(within(operations).getByText("Registrado")).toBeInTheDocument();
    expect(within(operations).getByText("Sin completar · No se pudo completar")).toBeInTheDocument();
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

  /** A close outcome the backend produced, rendered as the surface receives it. */
  function closes(overrides: Partial<ChatResponse> = {}): CloseConsultationResult {
    return {
      ok: true,
      response: {
        conversationId: "conversation-1",
        messageId: null,
        status: "registered",
        message: "He registrado en tu historia clínica el hecho que hablamos.",
        events: [],
        ...overrides,
      },
    };
  }

  /** Ends the consultation once the end button is actionable. */
  async function endConsultation(user: ReturnType<typeof userEvent.setup>) {
    const button = await screen.findByRole("button", { name: "Terminar consulta" });
    await waitFor(() => expect(button).toBeEnabled());
    await user.click(button);
  }

  it("ends the consultation and shows the close outcome", async () => {
    const user = userEvent.setup();
    closeConsultationMock.mockResolvedValue(
      closes({
        events: [
          {
            code: "evt_001",
            type: "diagnosis",
            date: "2026-01-10",
            datePrecision: "exact",
            content: "Hipertensión",
          },
        ],
      })
    );
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    expect(
      await screen.findByText("He registrado en tu historia clínica el hecho que hablamos.")
    ).toBeInTheDocument();
    expect(closeConsultationMock).toHaveBeenCalledWith("conversation-1");
  });

  it("exposes a busy state while closing and prevents a second close request", async () => {
    const user = userEvent.setup();
    closeConsultationMock.mockReturnValue(new Promise<CloseConsultationResult>(() => {}));
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    expect(await screen.findByText("Cerrando la consulta...")).toBeInTheDocument();
    const busy = screen.getByRole("button", { name: "Terminando..." });
    expect(busy).toBeDisabled();
    await user.click(busy);

    expect(closeConsultationMock).toHaveBeenCalledTimes(1);
  });

  it("shows the registered facts apart from the one that could not be registered", async () => {
    const user = userEvent.setup();
    const registeredEvent = {
      code: "evt_001",
      type: "diagnosis",
      date: "2026-01-10",
      datePrecision: "exact",
      content: "Hipertensión",
    };
    closeConsultationMock.mockResolvedValue(
      closes({
        message: "He registrado un hecho. No he podido registrar esto: algo.",
        events: [registeredEvent],
        operations: [
          { status: "registered", events: [registeredEvent] },
          { status: "failed", events: [] },
        ],
      })
    );
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    const operations = await screen.findByRole("list", { name: "Operaciones del turno" });
    expect(within(operations).getByText("Registrado")).toBeInTheDocument();
    expect(within(operations).getByText("Sin completar · No se pudo completar")).toBeInTheDocument();
  });

  it("does not offer to continue a closed consultation and lets starting a new one", async () => {
    const user = userEvent.setup();
    closeConsultationMock.mockResolvedValue(
      closes({
        status: "nothing_to_register",
        message: "Hemos cerrado la consulta. No había hechos médicos que registrar.",
      })
    );
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    expect(await screen.findByText("La consulta está cerrada.")).toBeInTheDocument();
    expect(screen.queryByLabelText("Mensaje para el asistente")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Terminar consulta" })).not.toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "Empezar una nueva consulta" }));

    expect(screen.getByLabelText("Mensaje para el asistente")).toBeInTheDocument();
    expect(screen.queryByText("La consulta está cerrada.")).not.toBeInTheDocument();
  });

  it("keeps the consultation internals out of the closed surface", async () => {
    const user = userEvent.setup();
    closeConsultationMock.mockResolvedValue(
      closes({ status: "nothing_to_register", message: "Hemos cerrado la consulta." })
    );
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    expect(await screen.findByText("La consulta está cerrada.")).toBeInTheDocument();
    expect(screen.queryByText(/enc_\d+/)).not.toBeInTheDocument();
    expect(screen.queryByText("conversation-1")).not.toBeInTheDocument();
    expect(screen.queryByText(/prompt|stack trace|credential/i)).not.toBeInTheDocument();
  });

  it("keeps a failed close retryable without leaking transport detail", async () => {
    const user = userEvent.setup();
    closeConsultationMock
      .mockResolvedValueOnce({ ok: false, errorCode: "UPSTREAM_FAILED" })
      .mockResolvedValueOnce(
        closes({ status: "nothing_to_register", message: "Hemos cerrado la consulta." })
      );
    sendChatMessageMock.mockResolvedValue(succeeds("noted", "Lo he anotado."));

    render(<ChatWorkspace />);
    await send(user, "Ayer tuve fiebre");
    await endConsultation(user);

    expect(
      await screen.findByText("No se pudo cerrar la consulta. Inténtalo de nuevo.")
    ).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("La consulta está cerrada.")).toBeInTheDocument();
    expect(closeConsultationMock).toHaveBeenCalledTimes(2);
    expect(screen.queryByText(/Failed to fetch/)).not.toBeInTheDocument();
  });
});