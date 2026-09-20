# Alinear la redacción del contrato del adaptador con el agente con operaciones

## Why

El cambio `uc-012` sustituyó el contrato de intenciones del adaptador por un conjunto de operaciones
declaradas, y su delta modificó el requisito que las produce. Dos requisitos del mismo spec conservaron,
sin embargo, la redacción del contrato anterior: uno sigue rechazando «unknown intent kinds» y
«responses containing both an answer and event candidates», que ya no existen, y otro describe la
réplica conversacional como una entrega aparte de la respuesta del turno.

El comportamiento que ambos requisitos protegen sigue siendo el correcto. Lo que quedó desfasado es su
letra, y un spec que describe un contrato retirado induce a error a quien lo lea como fuente de verdad
del comportamiento acordado.

## What Changes

- **Reescribir la descripción de «Validate provider responses before side effects»** para que rechace lo
  que el contrato actual sí puede producir: una respuesta ilegible, una operación fuera del conjunto
  declarado, un tipo de evento no admitido, un candidato sin un campo obligatorio y una respuesta que
  cita un hecho fuera del conjunto recuperado. Deja de rechazar «intenciones desconocidas» y respuestas
  que piden una operación y además traen texto, que es ahora el caso normal.
- **Precisar «Compose conversational replies outside the clinical history»** para declarar que esa
  réplica forma parte de la respuesta única del turno y no una entrega aparte. El material del que se
  compone y sus límites no cambian.

No cambia ningún comportamiento observable: es una alineación de redacción.

## Capabilities

- **Modified**: `llm-clinical-intent-adapter`

## Impact

- `openspec/specs/llm-clinical-intent-adapter/spec.md`: dos requisitos, sin cambio de escenarios.
- Sin cambios de código, de configuración ni de contrato HTTP.
