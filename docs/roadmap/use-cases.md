# Use Cases — MVP

> **Control de avance:** 7 de 11 implementados · 3 cubiertos · 1 pendiente.
>
> Implementación actual: **UC-001 → UC-004, UC-007, UC-008 y UC-010**.
>
> Sin documentar ni implementar: **UC-011**.

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

**Estado:** 🟡 Cubierto por UC-006 / UC-007

Cubierto como capacidad interna de recuperación: devolver un evento concreto identificado por su referencia no es observable por el usuario por sí mismo y sirve de base a la consulta de la historia (UC-007).

## UC-006 — Buscar información en la historia clínica

**Estado:** 🟡 Cubierto por UC-007

Cubierto como capacidad interna de recuperación: la búsqueda por texto, tipo y fecha no se ofrece de forma independiente y alimenta las respuestas de UC-007.

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

**Estado:** ⬜ Pendiente

Permite consultar los eventos almacenados para verificar o inspeccionar la información persistida.
