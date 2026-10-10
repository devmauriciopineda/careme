## Why

El MVP deja al paciente como una identidad implícita y no ofrece un lugar para mantener sus datos
personales básicos. La Fase 4.4 necesita materializar ese perfil sin convertir atributos vigentes en
hechos clínicos fechados ni obligar a la persona a completar una entrevista antes de poder usar el
asistente.

## What Changes

- Introducir un perfil del paciente para nombre y apellidos, fecha de nacimiento, sexo, ocupación, grupo sanguíneo, factor Rh y estatura.
- Proponer una entrevista inicial cuando el perfil esté vacío, permitiendo omitirla y retomarla posteriormente.
- Actualizar el perfil al cierre de la consulta con los datos claros y confirmados durante ella.
- Conservar únicamente el último valor confirmado de cada dato; los valores anteriores no forman parte del historial propio del perfil.
- Mantener ausentes los datos desconocidos, no aportados, ambiguos o no confirmados, sin completar con suposiciones.
- Pedir aclaración ante datos contradictorios o ambiguos y conservar el valor vigente anterior mientras no exista confirmación.
- Mantener separados los atributos del perfil de los hechos clínicos, las mediciones y el futuro perfil extendido.
- Mantener el perfil intacto si la actualización no puede completarse y permitir reintentarla.

## Capabilities

### New Capabilities

- `patient-profile`: construir y actualizar los datos personales vigentes del paciente, con entrevista inicial saltable y retomable.

### Modified Capabilities

Ninguna. Las capacidades existentes de conversación, registro de hechos y registro de mediciones no
cambian sus requisitos; el perfil se actualiza al cierre de la consulta mediante una capacidad nueva.

## Impact

- `backend/`: nuevo modelo y ciclo de actualización del perfil, validación de datos personales, lectura y escritura del perfil, integración con el cierre de la consulta y tratamiento de entrevista, aclaraciones y fallos.
- `frontend/`: presentación de la propuesta de entrevista, posibilidad de omitirla o retomarla y comunicación de confirmaciones y fallos sin exponer detalles internos.
- `backend/src/main/resources/prompts/`: ampliación del comportamiento conversacional para reconocer datos del perfil sin tratarlos como hechos clínicos.
- `docs/data-model.md`, `docs/architecture.md` y `docs/roadmap/use-cases.md`: actualización de la documentación cuando la capacidad quede implementada.
- Pruebas de backend, frontend y recorrido conversacional para perfil vacío, omisión, reanudación, corrección, aclaración y fallo de actualización.
- No requiere autenticación, autorización, historial de versiones del perfil ni perfil extendido; esas capacidades permanecen fuera de este cambio.
