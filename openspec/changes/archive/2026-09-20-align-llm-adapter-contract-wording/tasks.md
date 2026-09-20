# Tasks — Alinear la redacción del contrato del adaptador

## 1. Alineación

- [x] 1.1 Reescribir la descripción de «Validate provider responses before side effects» para rechazar lo que el contrato actual produce —una respuesta ilegible, una operación fuera del conjunto declarado, un tipo de evento no admitido, un candidato sin un campo obligatorio y una cita fuera del conjunto recuperado—, conservando sus tres escenarios.
  - Aplicado en el spec principal: el requisito ya no menciona «unknown intent kinds» ni «responses containing both an answer and event candidates», y sus tres escenarios se conservaron sin cambios.
- [x] 1.2 Precisar en «Compose conversational replies outside the clinical history» que la réplica conversacional forma parte de la respuesta única del turno y no una entrega aparte, conservando sus tres escenarios.
  - Aplicado en el spec principal: la descripción declara que esa réplica forma parte de la respuesta única del turno y que el sistema no la entrega como un segundo mensaje ni como una parte aparte; sus tres escenarios se conservaron sin cambios.
- [x] 1.3 Sincronizar el delta con `openspec/specs/llm-clinical-intent-adapter/spec.md` y verificar que el spec queda alineado y que los specs siguen siendo válidos.
  - `openspec validate --specs` → 7 passed, 0 failed. El spec del adaptador conserva sus 25 escenarios y no queda ningún resto del vocabulario del contrato de intenciones (`unknown intent`, `intent kind`, `containing both an answer`).
