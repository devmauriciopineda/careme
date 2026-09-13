## 1. Modelo y persistencia de eventos

- [x] 1.1 Crear el modelo canónico `ClinicalEvent` con UUID, código `evt_NNN`, tipo, contenido, fecha opcional, `date_precision`, `date_text`, fuente y fecha de creación; verificar invariantes con pruebas unitarias para los cuatro tipos admitidos y valores inválidos.
- [x] 1.2 Implementar el formato versionado de documentos Markdown en `data/events/` y su lector/escritor; verificar que un evento se serializa y deserializa conservando contenido, precisión temporal y expresión original.
- [x] 1.3 Crear la tabla o índice derivado de PostgreSQL sin modificar `measurements`; verificar la migración en una base limpia y que el esquema contiene `date_precision` y `date_text`.
- [x] 1.4 Implementar la reconstrucción del índice a partir de `data/events/`; verificar que puede regenerar el índice desde cero y que PostgreSQL no es necesario para recuperar la fuente primaria.

## 2. Interpretación y normalización

- [x] 2.1 Definir e integrar el contrato estructurado entre chat y registro para aceptar cero, uno o varios candidatos a evento, una solicitud de aclaración o conversación general; verificar el mapeo de cada tipo de intención al modelo canónico.
- [x] 2.2 Implementar la normalización temporal con el día actual como referencia, conservando únicamente `date_precision` y `date_text`; verificar fechas exactas, relativas, aproximadas y desconocidas sin inventar un día exacto.
- [x] 2.3 Implementar la validación de candidatos antes de cualquier escritura; verificar que se rechazan tipos no admitidos, contenido vacío, sospechas, preguntas, conversación general e información ambigua.

## 3. Registro y coordinación conversacional

- [x] 3.1 Implementar el caso de uso que valida todos los candidatos antes de persistir y registra un evento independiente por cada hecho; verificar el registro de uno y varios hechos en una sola interacción.
- [x] 3.2 Implementar la deduplicación basada en tipo, contenido normalizado y representación temporal dentro de la conversación actual; verificar que una repetición devuelve el evento existente y no crea otro.
- [x] 3.3 Implementar la escritura atómica de documentos e índice mediante temporales y publicación coordinada; verificar que un fallo no deja archivos, índices ni eventos parciales.
- [x] 3.4 Implementar resultados explícitos para éxito, duplicado, aclaración, mensaje no registrable y fallo; verificar que cada resultado se traduce a una respuesta breve en español sin detalles internos.

## 4. Verificación de integración

- [x] 4.1 Añadir pruebas del flujo completo desde la intención del chat hasta la persistencia y confirmación; verificar los escenarios de UC-004 incluidos en `specs/clinical-event-registration/spec.md`.
- [x] 4.2 Añadir una prueba de regresión que confirme que el registro de eventos no modifica el comportamiento ni el esquema existente de mediciones; ejecutar la suite backend y comprobar que pasa.
- [x] 4.3 Ejecutar validación OpenSpec y la verificación del proyecto; comprobar que `openspec validate "uc-004" --type change --strict --no-interactive` y los tests relevantes finalizan correctamente.
