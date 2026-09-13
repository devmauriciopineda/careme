## Context

El backend actual está organizado por capas y hoy persiste mediciones corporales
mediante PostgreSQL. El modelo propuesto del asistente introduce `ClinicalEvent`
para una única persona, con Markdown en `data/events/` como fuente primaria y un
índice textual derivado en PostgreSQL. La propuesta define el motivo y el alcance;
la especificación `clinical-event-registration` define el contrato observable.

El flujo de chat todavía debe coordinar interpretación de lenguaje natural,
validación y persistencia. Este cambio define el contrato estructurado entre el
chat y el registro clínico. La aplicación debe conservar las invariantes y no
delegar la integridad del registro al modelo de lenguaje.

## Goals / Non-Goals

**Goals:**

- Separar la interpretación del mensaje de la validación y persistencia del evento.
- Mantener un único modelo canónico de `ClinicalEvent` para registros simples y múltiples.
- Hacer que la escritura sea atómica: ningún fallo deja eventos parciales.
- Conservar fechas exactas, aproximadas, relativas y desconocidas sin aumentar su precisión.
- Permitir reconstruir el índice de PostgreSQL desde los archivos Markdown.
- Mantener el flujo preparado para las consultas de UC-005 a UC-008 sin implementarlas.

**Non-Goals:**

- No añadir autenticación, múltiples pacientes ni selección de propietario.
- No implementar búsqueda, consulta, edición o borrado de eventos.
- No modelar diagnósticos, medicaciones o mediciones como entidades clínicas especializadas.
- No aceptar documentos, imágenes, audio ni recomendaciones médicas.
- No convertir al LLM en fuente de verdad ni permitir que escriba directamente en la persistencia.

## Decisions

### Contrato estructurado entre chat y registro

El chat entregará una intención estructurada con un resultado explícito:
`events` con cero o más candidatos, `clarification` cuando falte información,
o `conversation` cuando el mensaje no sea un hecho registrable. Cada candidato
contendrá tipo, contenido, fecha opcional, `date_precision` y `date_text` cuando
corresponda. El contrato será independiente del proveedor de lenguaje y será el
único punto de entrada del caso de uso.

Alternativa descartada: acoplar el registro a texto libre o a la respuesta
directa del proveedor. Impide validar de forma determinista y mezcla conversación
con persistencia.

### La aplicación valida antes de persistir

El adaptador del contrato producirá cero, uno o varios candidatos a evento, o una
solicitud de aclaración. Una capa de dominio
validará tipo, contenido, pertenencia al paciente implícito, precisión temporal y
campos obligatorios antes de cualquier escritura. Se elige este límite porque
protege las invariantes aunque cambie el proveedor o prompt de interpretación.

Alternativa descartada: permitir que el LLM genere Markdown directamente. Reduce
código inicial, pero permite registros mal formados, precisión temporal inventada
y escrituras parciales difíciles de probar.

### Markdown como fuente primaria y PostgreSQL como índice derivado

Cada evento se escribirá como un documento Markdown con front matter estable,
`code` legible (`evt_NNN`), UUID, tipo, fecha, precisión, expresión original,
fuente y fecha de creación. PostgreSQL mantendrá los metadatos necesarios para
búsqueda textual y podrá reconstruirse leyendo `data/events/`.

La escritura de un mensaje con varios hechos usará una operación coordinada:
validar todos los candidatos, escribir los documentos temporales, actualizar el
índice y publicar los archivos solo cuando el conjunto sea válido. Si un paso
falla, se eliminarán temporales y no se publicará ningún evento nuevo.

Alternativa descartada: usar PostgreSQL como fuente primaria. Simplifica
transacciones, pero contradice el modelo del MVP y hace que los registros sean
menos portables y trazables como documentos humanos.

### Resolución temporal explícita

Un componente de normalización recibirá el texto original y el día de referencia
actual. Para expresiones relativas producirá una fecha calculada más
`date_precision: exact` cuando la expresión lo permita y conservará `date_text`.
Para expresiones aproximadas o ausentes conservará únicamente
`date_precision` y `date_text`, sin introducir rangos ni un día inventado; la
confirmación usará la expresión original cuando sea necesario.

Alternativa descartada: guardar solo una fecha ISO. Perdería incertidumbre y
haría imposible cumplir la fidelidad temporal del caso de uso.

### Duplicación limitada a la conversación actual

El coordinador de conversación conservará las identidades o huellas de los
eventos registrados durante la conversación. Antes de crear un candidato nuevo,
comparará el contenido normalizado, tipo y representación temporal contra ese
conjunto. Una repetición devuelve la confirmación del evento existente sin
consultar ni modificar registros históricos.

Alternativa descartada: deduplicación global por similitud textual. Podría
fusionar hechos legítimos ocurridos en momentos distintos y queda fuera del
alcance de UC-004.

### Respuestas visibles separadas de detalles internos

El flujo tendrá resultados explícitos para registro exitoso, aclaración, mensaje
no registrable, duplicado y fallo. Un traductor de resultados construirá mensajes
breves en español; los errores técnicos se registrarán internamente y no se
expondrán en la conversación.

## Risks / Trade-offs

- **[Interpretación incorrecta]** El lenguaje natural puede producir candidatos erróneos → validar estructura e invariantes y pedir aclaración cuando la confianza o información mínima no alcance.
- **[Resolución temporal ambigua]** Algunas expresiones no corresponden a un único día → conservar `date_text`, marcar precisión aproximada o desconocida y no inventar una fecha exacta.
- **[Desincronización índice/archivos]** Un fallo entre escritura de Markdown e índice puede dejar estados distintos → usar escritura temporal, publicación atómica y una operación explícita de reconstrucción del índice.
- **[Detección de duplicados limitada]** La huella de conversación no evita repeticiones en conversaciones futuras → limitar la garantía a la conversación actual, como exige UC-004, y dejar deduplicación global para una decisión posterior.
- **[Evolución del formato Markdown]** Cambios de front matter pueden romper reconstrucción → versionar el formato y probar lectura de documentos válidos y heredados antes de cambiarlo.

## Migration Plan

1. Crear el directorio de datos y el formato versionado de documentos `ClinicalEvent`.
2. Añadir la migración o tabla de índice derivado sin modificar la tabla existente de mediciones.
3. Implementar la reconstrucción del índice desde `data/events/` antes de activar el flujo de registro.
4. Activar el registro detrás del flujo de chat y verificar escritura atómica con datos vacíos y múltiples eventos.
5. Rollback: desactivar el flujo de registro y conservar los documentos ya publicados; reconstruir o eliminar únicamente el índice derivado sin tocar `measurements`.

