## Why

El registro de hechos clínicos falla siempre en el despliegue con contenedores. `ClinicalEventMarkdownStore` escribe en `data/events` relativo al directorio de trabajo `/app`, que pertenece a root, mientras el backend corre como usuario `careme`; `Files.createDirectories` lanza `AccessDeniedException`, el registro se revierte y la persona solo ve "No se pudo registrar el hecho. Puedes volver a intentarlo.". Además, el directorio Markdown —fuente de verdad de la historia clínica— vive en la capa efímera del contenedor y no está montado: cada recreación pierde los documentos, y el reconciliador de arranque reconstruye el índice derivado desde un directorio vacío, borrando la historia.

## What Changes

- Hacer configurable el directorio de la fuente de verdad Markdown (`careme.events.directory`, por defecto `data/events`), para que el despliegue elija una ruta escribible sin cambiar el código.
- Crear el directorio de datos escribible por el usuario no-root del backend dentro de la imagen.
- Montar un volumen nombrado para el directorio Markdown, de modo que los documentos sobrevivan a la recreación del contenedor y el índice derivado pueda reconstruirse.
- Registrar en el log del servidor la causa del fallo de persistencia, manteniendo el mensaje a la persona libre de detalles internos.
- Asignar los códigos de evento a partir de los documentos persistidos, para que un reinicio del backend no reutilice un código ya publicado.
- Publicar documentos con semántica de solo creación: nunca sobrescribir ni borrar el documento de un hecho ya registrado, y revertir únicamente lo que el intento creó.
- Fuera de alcance: cambios al reconciliador o a la estrategia del índice derivado, `.gitignore` de los datos de desarrollo local, y cualquier cambio de contrato HTTP, DTOs o migraciones.

## Capabilities

### New Capabilities

_(ninguna)_

### Modified Capabilities

- `clinical-event-registration`: la ubicación de la fuente de verdad Markdown pasa de estar fijada a ser configurable, el despliegue MUST garantizar que sea escribible por el usuario del proceso y durable entre recreaciones del contenedor, y el fallo de persistencia MUST quedar registrado en el servidor. Además, los códigos de evento MUST asignarse desde los documentos persistidos y MUST NOT reutilizarse, y un fallo de registro MUST NOT destruir hechos ya registrados.

## Impact

- Backend Spring Boot: `ClinicalEventMarkdownStore` (directorio desde configuración, publicación de solo creación y reserva de códigos), `ClinicalEventRegistrationService` (log de la causa y códigos reservados), `application.yml` (propiedad nueva) y `Dockerfile` (directorio de datos con dueño del usuario del proceso).
- Despliegue: `docker-compose.yml` gana un volumen nombrado para la fuente de verdad; nueva variable de entorno opcional `CAREME_EVENTS_DIRECTORY`.
- Pruebas: unitarias del almacén con directorio temporal; verificación manual del registro dentro del contenedor.
- Sin cambios en la API, en el contrato conversacional, en el esquema de base de datos ni en el comportamiento visible, salvo que el registro deja de fallar.
