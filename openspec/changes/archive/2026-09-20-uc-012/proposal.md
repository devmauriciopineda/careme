## Why

El asistente del MVP decide el cauce de cada mensaje una sola vez y produce un solo resultado: registra,
consulta, declara la ausencia o conversa. Un mensaje que necesita más de una cosa —contar un hecho y
preguntar por la historia en la misma frase, o una pregunta que exige más de una búsqueda— no puede
atenderse completo, y el modelo no decide qué hacer: devuelve una intención y el backend elige la
operación (`mvp_alcance_asistente_historia_clinica.md` §8).

El comportamiento por defecto del producto es además el modo simulado, una heurística de desarrollo que
acierta por reglas y no por comprensión (`mvp_alcance` §16): la persona no habla con el asistente real
salvo que alguien lo active explícitamente. La Fase 4.2 cierra las dos cosas juntas, porque la segunda es
la que hace verificable la primera.

## What Changes

- El asistente **decide qué necesita hacer** para atender el mensaje y ejecuta las operaciones que
  determina —consultar la historia y registrar hechos— dentro del mismo intercambio, sirviéndose del
  resultado de una para decidir la siguiente.
- El **conjunto de operaciones disponibles** acota lo que el asistente puede hacer: consultar y registrar.
  Nada más está a su alcance y una operación fuera de ese conjunto no se ejecuta.
- La comprobación y la escritura ocurren **dentro del camino de la operación**: ningún hecho se incorpora
  a la historia sin validación, con independencia de cómo se haya decidido la operación.
- La persona recibe **una sola respuesta** que cubre todo lo que su mensaje requería, sin ver las
  operaciones ejecutadas ni confirmarlas una a una.
- Un **fallo parcial no deshace lo completado**: la historia conserva lo que ya quedó registrado y la
  respuesta informa de lo registrado y de lo que no pudo completarse.
- Cuando falte información para completar una operación, el asistente **pide lo que falta** y no ejecuta
  ni inventa la operación.
- El **asistente real pasa a ser el comportamiento por defecto** del producto; el modo simulado queda como
  modo de desarrollo y de pruebas y nunca sustituye al asistente real en silencio.
- Si el asistente no está disponible, se informa de un **fallo recuperable**, ninguna operación se ejecuta
  y la historia no cambia.
- **BREAKING**: el contrato con el proveedor deja de ser «una intención por mensaje» y pasa a ser «las
  operaciones que el asistente decide»; el resultado del turno pasa a informar de más de una operación.

## Capabilities

### New Capabilities

- `assistant-agent-turn`: el asistente decide qué operaciones acotadas necesita el mensaje, las ejecuta
  dentro del mismo intercambio con validación y escritura en el camino de la operación, y entrega una sola
  respuesta que cubre todo lo pedido, informando de lo que quedó registrado y de lo que no pudo completarse.

### Modified Capabilities

- `llm-clinical-intent-adapter`: la frontera deja de mapear una intención única por mensaje y pasa a
  ejecutar las operaciones que el asistente pide, validando cada una antes de producir efecto; se añade la
  selección de proveedor con el asistente real por defecto.
- `assistant-conversation`: el turno deja de ser «un resultado por mensaje» y pasa a informar de las
  operaciones completadas y de la que no se completó, conservando el catálogo de resultados y su contenido.
- `clinical-event-registration`: el registro puede originarse como operación elegida por el asistente y su
  validación determinista ocurre dentro del camino de la operación; un fallo posterior en el mismo turno no
  revierte un registro ya completado.
- `assistant-chat-interface`: la presentación debe mostrar un turno con más de una operación y un turno
  parcialmente completado, distinguiendo lo que quedó registrado de lo que no pudo completarse.

## Impact

- **Backend**: se sustituye la interpretación por intención única por un bucle de operaciones acotadas;
  `ChatOrchestrator` deja de resolver el cauce y pasa a ejecutar y componer; los adaptadores de proveedor,
  real y simulado, se rehacen sobre la nueva frontera; se añade la selección de proveedor con el real por
  defecto.
- **Contrato de chat**: la respuesta incorpora el detalle de las operaciones del turno. Es un cambio
  aditivo sobre un contrato sin autenticación ni consumidores externos; los resultados existentes no se
  renombran ni cambian de significado.
- **Frontend**: `features/chat` amplía su esquema de validación y `ChatWorkspace` representa el turno con
  varias operaciones y el turno parcialmente completado.
- **Configuración**: `CAREME_LLM_MODE` deja de tener el modo simulado por defecto en `.env.example`,
  `docker-compose.yml` y `backend/src/main/resources/application.yml`. El modo simulado se conserva para
  desarrollo y pruebas, conforme a `backend-tests.instructions.md`.
- **Prompts**: nuevas versiones de los prompts de interpretación y de composición, versionadas como
  archivos.
- **Documentación**: se actualizan `docs/concepts/05-ia-llm-prompts-y-rag.md`, `docs/architecture.md`,
  `docs/concepts/08-despliegue.md`, los README y las notas de revisión de `mvp_alcance` §8 y §16, y
  `docs/use-cases/UC-012.md` pasa a implementado al archivar el cambio.
- **Fuera de alcance**: la consulta como sesión con identificador y la procedencia de los hechos (UC-013),
  el modelo de mediciones con dimensión de métrica (UC-014), el perfil del paciente (UC-015), los tipos
  nuevos de evento (UC-016 a UC-019), las condiciones y relaciones (UC-020 a UC-022) y el perfil extendido
  (UC-024). También quedan fuera la actuación autónoma fuera del mensaje que la origina, la ejecución en
  segundo plano y los flujos multi-etapa (`mvp_alcance` §14).
