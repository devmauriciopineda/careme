"use client";

import Link from "next/link";
import { ArrowUp, HeartPulse, LoaderCircle, RotateCcw, Scale } from "lucide-react";
import { FormEvent, useState, useTransition } from "react";

import { sendChatMessage } from "../actions";
import type { ChatResponse } from "../types";

type Message = {
  id: string;
  role: "user" | "assistant";
  text: string;
  status?: ChatResponse["status"];
  events?: ChatResponse["events"];
  retryText?: string;
};

const statusLabels: Record<NonNullable<Message["status"]>, string> = {
  registered: "Registrado",
  answered: "Respuesta",
  no_records: "Sin registros",
  clarification_required: "Necesita aclaración",
  general_conversation: "Conversación",
  duplicate: "Ya estaba registrado",
  failed: "No se pudo completar",
};

/**
 * Names the precision the backend reported instead of presenting every date as
 * exact, so an approximate or unknown date stays approximate or unknown.
 */
const precisionLabels: Record<string, string> = {
  exact: "fecha exacta",
  approximate: "fecha aproximada",
  unknown: "fecha no registrada",
};

function eventSummary(event: ChatResponse["events"][number]): string {
  const precision = precisionLabels[event.datePrecision] ?? event.datePrecision;
  return event.date ? `${event.date} (${precision})` : precision;
}

/**
 * Shown when a turn is not sent. It says what happened without naming a cause,
 * because the cause is an implementation detail the user cannot act on.
 */
const SEND_FAILED_MESSAGE = "No se pudo completar el envío. Inténtalo de nuevo.";

export function ChatWorkspace() {
  const [conversationId, setConversationId] = useState<string>();
  const [draft, setDraft] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);
  const [isPending, startTransition] = useTransition();

  /** Keeps the failed turn available so the retry can send it again. */
  function recordFailure(messageId: string, text: string) {
    setMessages((current) => [
      ...current,
      {
        id: `${messageId}-error`,
        role: "assistant",
        text: SEND_FAILED_MESSAGE,
        status: "failed",
        retryText: text,
      },
    ]);
  }

  function submit(event: FormEvent, text = draft) {
    event.preventDefault();
    const message = text.trim();
    if (!message || isPending) return;

    const messageId = crypto.randomUUID();
    setDraft("");
    setMessages((current) => [...current, { id: messageId, role: "user", text: message }]);

    startTransition(async () => {
      let outcome: Awaited<ReturnType<typeof sendChatMessage>>;

      try {
        outcome = await sendChatMessage({ message, conversationId, messageId });
      } catch (error) {
        console.error("Failed to reach the chat action:", error);
        recordFailure(messageId, message);
        return;
      }

      if (!outcome.ok) {
        console.error(`Chat turn was not sent: ${outcome.errorCode}`);
        recordFailure(messageId, message);
        return;
      }

      setConversationId(outcome.response.conversationId);
      setMessages((current) => [
        ...current,
        {
          id: `${messageId}-assistant`,
          role: "assistant",
          text: outcome.response.message,
          status: outcome.response.status,
          events: outcome.response.events,
        },
      ]);
    });
  }

  return (
    <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col px-4 py-5 sm:px-6 lg:px-8 lg:py-8">
      <header className="flex items-center justify-between border-b border-border/70 pb-5">
        <div className="flex items-center gap-3">
          <div className="flex size-10 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm">
            <HeartPulse aria-hidden="true" className="size-5" />
          </div>
          <div>
            <p className="text-sm font-semibold tracking-tight">Careme</p>
            <p className="text-xs text-muted-foreground">Historia clínica personal</p>
          </div>
        </div>
        <Link className="inline-flex items-center gap-2 rounded-lg border border-border px-3 py-2 text-sm font-medium transition hover:bg-muted" href="/measurements">
          <Scale aria-hidden="true" className="size-4" />
          Mediciones
        </Link>
      </header>

      <section className="grid flex-1 gap-8 py-8 lg:grid-cols-[minmax(0,1fr)_260px]">
        <div className="flex min-h-[520px] flex-col">
          <div className="mb-8 max-w-2xl">
            <p className="mb-3 text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">Asistente</p>
            <h1 className="text-3xl font-semibold tracking-tight sm:text-4xl">Cuéntame qué quieres dejar registrado.</h1>
            <p className="mt-3 max-w-xl text-muted-foreground">Puedes escribirlo con tus propias palabras. Mantendré la fecha tal como la recuerdas.</p>
          </div>

          <div aria-live="polite" className="flex-1 space-y-4 overflow-y-auto rounded-2xl border border-border/70 bg-card/70 p-4 shadow-sm sm:p-6">
            {messages.length === 0 ? (
              <div className="flex h-full min-h-64 items-center justify-center text-center text-sm text-muted-foreground">
                <p>Ejemplo: “Ayer me diagnosticaron hipertensión”.</p>
              </div>
            ) : messages.map((message) => (
              <div className={message.role === "user" ? "ml-auto max-w-[85%]" : "max-w-[85%]"} key={message.id}>
                <div className={message.role === "user" ? "rounded-2xl rounded-br-sm bg-primary px-4 py-3 text-sm text-primary-foreground" : "rounded-2xl rounded-bl-sm bg-muted px-4 py-3 text-sm"}>
                  {message.text}
                </div>
                {message.role === "assistant" && message.status && (
                  <p className="mt-1 text-xs text-muted-foreground">{statusLabels[message.status]}</p>
                )}
                {message.role === "assistant" && message.events && message.events.length > 0 && (
                  <ul aria-label="Hechos que sustentan la respuesta" className="mt-2 space-y-1">
                    {message.events.map((event) => (
                      <li className="rounded-lg border border-border/70 bg-card px-3 py-2 text-xs text-muted-foreground" key={event.code}>
                        <span className="font-semibold text-foreground">{event.code}</span>
                        <span> · {eventSummary(event)}</span>
                        <p className="mt-1 text-foreground">{event.content}</p>
                      </li>
                    ))}
                  </ul>
                )}
                {message.status === "failed" && message.retryText && (
                  <button className="mt-2 inline-flex items-center gap-1 text-xs font-semibold text-destructive hover:underline" onClick={(event) => submit(event, message.retryText)} type="button">
                    <RotateCcw aria-hidden="true" className="size-3" /> Reintentar
                  </button>
                )}
              </div>
            ))}
            {isPending && <div className="flex items-center gap-2 text-sm text-muted-foreground" role="status"><LoaderCircle aria-hidden="true" className="size-4 animate-spin" /> Procesando tu mensaje...</div>}
          </div>

          <form className="mt-4 flex items-end gap-2 rounded-2xl border border-border bg-card p-2 shadow-sm" onSubmit={submit}>
            <label className="sr-only" htmlFor="chat-message">Mensaje para el asistente</label>
            <textarea className="min-h-12 flex-1 resize-none border-0 bg-transparent px-3 py-3 text-sm outline-none placeholder:text-muted-foreground" disabled={isPending} id="chat-message" onChange={(event) => setDraft(event.target.value)} placeholder="Escribe un hecho médico..." value={draft} />
            <button aria-label="Enviar mensaje" className="flex size-11 shrink-0 items-center justify-center rounded-xl bg-primary text-primary-foreground transition hover:opacity-90 disabled:cursor-not-allowed disabled:opacity-40" disabled={!draft.trim() || isPending} title="Enviar mensaje" type="submit">
              <ArrowUp aria-hidden="true" className="size-5" />
            </button>
          </form>
        </div>

        <aside className="hidden border-l border-border/70 pl-6 lg:block">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">Tu espacio</p>
          <p className="mt-3 text-sm leading-6 text-muted-foreground">Los hechos registrados pasan a formar parte de tu historia clínica. Las preguntas generales no crean registros.</p>
        </aside>
      </section>
    </main>
  );
}