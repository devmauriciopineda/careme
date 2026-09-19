# Use Cases

> **MVP cerrado el 2026-09-18.** 8 de 11 implementados · 3 cubiertos.
>
> Implementación actual: **UC-001 → UC-004, UC-007, UC-008, UC-010 y UC-011**.
>
> Todos los casos de uso del MVP están implementados o cubiertos.
>
> Los casos de uso de la Fase 4 se enuncian al final del documento, en un bloque que señala que **no
> forman parte del MVP**.

Leyenda de estados:

| Estado | Significado |
| --- | --- |
| ✅ Implementado | Funcionalidad completa en backend y frontend |
| 🟡 Cubierto | Cubierto por otro use case como flujo alterno |
| 📝 Documentado | Caso de uso y criterios de aceptación redactados, sin implementar |
| ⬜ Pendiente | No implementado ni documentado |

Frontera de las respuestas del asistente: **UC-007** responde sobre la historia
clínica, **UC-008** declara la ausencia de registros clínicos y **UC-010** responde
como conversación general sin consultar la historia. Cada uno documenta la
frontera con los otros dos en su sección 11.

## UC-001 — Ver el seguimiento corporal

**Estado:** ✅ Implementado

Permite al usuario conocer la evolución de su peso y de su cintura abdominal como tendencia y como valores por fecha.

## UC-002 — Registrar la medición del día

**Estado:** ✅ Implementado

Permite al usuario dejar constancia de su peso y su circunferencia abdominal de un día concreto.

## UC-003 — Cargar mediciones desde un archivo

**Estado:** ✅ Implementado

Permite al usuario incorporar de una sola vez el histórico de mediciones contenido en un archivo.

## UC-004 — Registrar un evento clínico

**Estado:** ✅ Implementado

Permite al usuario registrar un hecho médico expresado en lenguaje natural.

## UC-005 — Consultar un evento clínico

**Estado:** 🟡 Cubierto por UC-011 / UC-007

Capacidad interna de lectura: devolver un evento concreto identificado por su código. No es una
herramienta del LLM. Se expone como `GET /api/v1/clinical-events/{code}` y su observabilidad para el
usuario proviene de la vista de inspección (UC-011); como base de una respuesta, la cubre la consulta
de la historia (UC-007).

## UC-006 — Buscar información en la historia clínica

**Estado:** 🟡 Cubierto por UC-007

Capacidad interna de recuperación: la búsqueda por texto, tipo y fecha no se ofrece de forma
independiente ni como herramienta del LLM, y alimenta las respuestas de UC-007. Su contrato es
`search_events` (alcance §8.2).

## UC-007 — Consultar la historia clínica mediante lenguaje natural

**Estado:** ✅ Implementado

Permite al usuario realizar preguntas sobre su historia clínica y obtener respuestas basadas en la información registrada.

Documento: [`UC-007.md`](../use-cases/UC-007.md) · [criterios de aceptación](../use-cases/UC-007-acceptance-criteria.md).

## UC-008 — Gestionar información clínica ausente

**Estado:** ✅ Implementado

Permite al sistema responder adecuadamente cuando no existen registros que respalden la información solicitada.

Documento: [`UC-008.md`](../use-cases/UC-008.md) · [criterios de aceptación](../use-cases/UC-008-acceptance-criteria.md).

## UC-009 — Registrar información temporalmente imprecisa

**Estado:** 🟡 Cubierto por UC-004

Cubierto por UC-004 como flujo alterno: el hecho se registra conservando la precisión temporal que aporta el usuario.

## UC-010 — Consultar información que no pertenece a la historia clínica

**Estado:** ✅ Implementado

Permite al usuario realizar preguntas de carácter general que no requieren consultar los registros de su historia clínica, obtener una respuesta conversacional y recibir una negativa explícita cuando pide un diagnóstico, una recomendación o una interpretación de su caso.

Documento: [`UC-010.md`](../use-cases/UC-010.md) · [criterios de aceptación](../use-cases/UC-010-acceptance-criteria.md).

## UC-011 — Inspeccionar los eventos clínicos registrados

**Estado:** ✅ Implementado

Permite consultar los eventos almacenados para verificar o inspeccionar la información persistida.

Documento: [`UC-011.md`](../use-cases/UC-011.md) · [criterios de aceptación](../use-cases/UC-011-acceptance-criteria.md).

---

## Fase 4 — no forman parte del MVP

Casos de uso previstos para la Fase 4 del
[`roadmap_asistente_historia_clinica.md`](./roadmap_asistente_historia_clinica.md): tres habilitadores
—agente con herramientas, consulta y modelo de mediciones—, el perfil del paciente y la ampliación del
modelo clínico.

Se enuncian aquí para fijar el alcance de la fase. **Los documentos de caso de uso y sus criterios de
aceptación están pendientes de escribir.**

## UC-012 — Operar el asistente como agente con herramientas y proveedor LLM real por defecto

**Estado:** ⬜ Pendiente

El asistente decide qué necesita hacer y llama herramientas para consultar o registrar, y el backend valida dentro de la llamada.

## UC-013 — Sostener una consulta y registrar con procedencia los hechos que el usuario menciona

**Estado:** ⬜ Pendiente

La conversación es una consulta con identificador; el asistente pregunta lo que falta y los hechos quedan registrados con procedencia hacia ella.

## UC-014 — Registrar y consultar mediciones por lenguaje natural

**Estado:** ⬜ Pendiente

Peso, circunferencia abdominal, presión arterial y colesterol, sobre un modelo con dimensión de métrica referenciado a la consulta.

## UC-015 — Construir y actualizar el perfil del paciente

**Estado:** ⬜ Pendiente

Entrevista inicial saltable y retomable, y actualización cuando el usuario reporta un cambio; el perfil conserva únicamente los valores vigentes.

## UC-016 — Registrar síntomas

**Estado:** ⬜ Pendiente

Registrar un síntoma expresado en lenguaje natural, conservando su fecha y su precisión temporal.

## UC-017 — Registrar procedimientos, hospitalizaciones y estudios

**Estado:** ⬜ Pendiente

Registrar los hechos de atención recibida, separando el procedimiento, la hospitalización y el estudio.

## UC-018 — Registrar vacunas y alergias

**Estado:** ⬜ Pendiente

Registrar vacunaciones y alergias o intolerancias como hechos con fecha, precisión y procedencia.

## UC-019 — Consultar la historia por los nuevos tipos de evento

**Estado:** ⬜ Pendiente

Extender el retrieval del chat y los filtros de la inspección a los tipos clínicos que añade la fase, entre ellos los antecedentes familiares.

## UC-020 — Agrupar hechos en una condición y consultar su evolución

**Estado:** ⬜ Pendiente

Reunir en una condición los hechos que la describen y responder preguntas sobre su evolución.

## UC-021 — Gestionar el estado de una condición

**Estado:** ⬜ Pendiente

Mantener el estado de una condición: activa, resuelta, sospechada, descartada, recurrente o desconocida.

## UC-022 — Vincular hechos clínicos relacionados

**Estado:** ⬜ Pendiente

Relacionar condiciones, eventos, medicamentos y estudios entre sí.

## UC-023 — Consultar la procedencia de un hecho registrado

**Estado:** ⬜ Pendiente

Responder de dónde salió un hecho: quién lo aportó, en qué consulta y con qué nivel de confianza.

## UC-024 — Generar el perfil extendido con los hechos médicos principales

**Estado:** ⬜ Pendiente

Componer automáticamente un documento derivado con el perfil del paciente y sus hechos principales: condiciones activas, alergias, medicación vigente, últimas mediciones y antecedentes familiares.

---

### Revisión prevista de los casos de uso del MVP

La Fase 4 revisa UC-001, UC-002 y UC-003 (las mediciones dejan de vivir en su tabla propia) y UC-004,
UC-007 y UC-011 (tipos nuevos, procedencia y registro desde la consulta). No se reescriben aquí.
