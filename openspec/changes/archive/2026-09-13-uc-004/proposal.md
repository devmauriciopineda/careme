## Why

Careme necesita convertir los hechos médicos que la persona cuenta en el chat
en entradas consultables de su historia clínica. Este es el punto de entrada del
MVP del asistente: sin registro fiel, temporalmente preciso y sin duplicados, las
funcionalidades posteriores de consulta no tienen una fuente de verdad fiable.

## What Changes

- Añadir el registro de uno o varios hechos clínicos expresados en lenguaje natural.
- Definir e integrar el contrato estructurado entre el chat y el registro clínico.
- Clasificar cada hecho como diagnóstico, medicación, medición o nota.
- Extraer y conservar el contenido, la expresión temporal original y la precisión
  de la fecha: exacta, aproximada o desconocida.
- Resolver expresiones relativas usando el día actual sin inventar precisión.
- Pedir aclaración para mensajes ambiguos y no registrar creencias, sospechas,
  preguntas o conversación general como hechos.
- Evitar duplicados del mismo hecho dentro de la conversación en curso.
- Confirmar brevemente los registros exitosos en español y mantener la historia
  sin cambios cuando el registro falla.

## Capabilities

### New Capabilities

- `clinical-event-registration`: registrar hechos clínicos propios desde el chat,
  preservando su contenido y precisión temporal.

### Modified Capabilities

Ninguna.

## Impact

- Backend: nuevo caso de uso de registro, dominio `ClinicalEvent`, validación,
  persistencia primaria de eventos y coordinación con el flujo de conversación.
- Datos: creación de documentos de eventos en `data/events/` y del índice
  derivado reconstruible en PostgreSQL, según el modelo propuesto.
- Contrato de interacción: respuestas de confirmación, aclaración y error en
  español; no se añaden endpoints de consulta dentro de este cambio.
- Dependencias posteriores: UC-005 a UC-008 podrán consultar los eventos, pero
  quedan fuera de alcance aquí.
