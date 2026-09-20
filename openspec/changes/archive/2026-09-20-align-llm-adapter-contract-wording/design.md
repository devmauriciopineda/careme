# Design — Alinear la redacción del contrato del adaptador

## Context

`uc-012` cambió el contrato del adaptador de intenciones a operaciones declaradas y modificó el
requisito que las produce. Dos requisitos del mismo spec no formaban parte de aquel delta y conservaron
la redacción del contrato anterior, de modo que el spec describe a la vez el contrato vigente y el
retirado.

## Decisions

### D1 — Alinear la redacción en lugar de dejar la deuda

Un spec es la fuente de verdad del comportamiento acordado. Una descripción que menciona «intenciones
desconocidas» o una entrega conversacional aparte describe un sistema que ya no existe, y quien la lea
—una persona o un agente que trabaje sobre el repositorio— puede deducir de ella reglas falsas. El coste
de alinearla es de redacción; el de no hacerlo es una fuente de verdad que engaña.

### D2 — No cambiar el comportamiento que los requisitos protegen

Se conservan intactos los escenarios existentes y sus garantías: nada ilegible llega a la persistencia,
una respuesta no se presenta como fundamentada cuando cita hechos que no se recuperaron, la réplica
conversacional no lee la historia y no se usa para responder sobre ella. Solo se ajusta la descripción
de cada requisito.

### D3 — Un cambio propio en lugar de un retoque a mano

Los specs principales solo cambian por el flujo de trabajo. Se abre este cambio para que la alineación
quede registrada con su motivo y su procedencia, en lugar de aparecer como una edición directa del spec
sin cambio que la respalde.

## Risks / Trade-offs

- **[Ningún riesgo sobre el comportamiento]** → Ningún escenario cambia de contenido ni se añade o
  elimina una garantía. El riesgo es de redacción y se acota comprobando, tras sincronizar, que cada
  escenario del spec principal sigue presente y que no queda vocabulario del contrato de intenciones.
