# Use Cases — MVP

> **Control de avance:** 5 de 11 implementados · 3 cubiertos · 3 pendientes.
>
> Implementación actual: **UC-001 → UC-004 y UC-007**.
>
> Documentado y pendiente de implementación: **UC-007**.

Leyenda de estados:

| Estado | Significado |
| --- | --- |
| ✅ Implementado | Funcionalidad completa en backend y frontend |
| 🟡 Cubierto | Cubierto por otro use case como flujo alterno |
| ⬜ Pendiente | No implementado |

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

**Estado:** ⬜ Pendiente

Permite al sistema responder adecuadamente cuando no existen registros que respalden la información solicitada.

## UC-009 — Registrar información temporalmente imprecisa

**Estado:** 🟡 Cubierto por UC-004

Cubierto por UC-004 como flujo alterno: el hecho se registra conservando la precisión temporal que aporta el usuario.

## UC-010 — Consultar información que no pertenece a la historia clínica

**Estado:** ⬜ Pendiente

Permite al usuario realizar preguntas de carácter general que no requieren consultar los registros de su historia clínica.

## UC-011 — Inspeccionar los eventos clínicos registrados

**Estado:** ⬜ Pendiente

Permite consultar los eventos almacenados para verificar o inspeccionar la información persistida.
