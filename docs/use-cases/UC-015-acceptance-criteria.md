# UC-015 — Construir y actualizar el perfil del paciente — Acceptance Criteria

## Feature

**ID:** UC-015  
**Use case:** Construir y actualizar el perfil del paciente  
**Source:** [UC-015.md](./UC-015.md)

La persona puede completar o actualizar sus datos personales básicos durante una consulta. Al cerrarse
la consulta, el perfil conserva únicamente los valores vigentes que la persona confirmó; la entrevista
puede omitirse y retomarse.

## Execution rules

- Los escenarios son independientes y pueden ejecutarse en cualquier orden.
- `Given` describe las precondiciones y las reglas de negocio relevantes.
- `When` describe la acción de la persona o el evento que inicia el flujo.
- `Then` describe las respuestas observables y el estado resultante.
- `And` amplía el paso anterior sin introducir una fase nueva.
- Cada escenario referencia el flujo, la excepción o la regla de origen mediante `Ref`, usando los
  identificadores de [`reglas-de-negocio.md`](./reglas-de-negocio.md) cuando corresponda.

## Scenarios

### Scenario: Actualizar el perfil al cerrar una consulta

**Ref:** Flujo principal / UC-015-R2, UC-015-R6, UC-015-R7

Given existe una consulta en curso y la persona ha confirmado uno o varios datos personales básicos  
When la consulta se cierra  
Then el sistema actualiza el perfil con los datos confirmados  
And conserva únicamente el último valor confirmado de cada dato  
And deja ausentes los datos que la persona no aportó o no confirmó  
And la persona puede continuar usando el asistente sin completar todos los datos del perfil

### Scenario: Proponer la entrevista inicial sin hacerla obligatoria

**Ref:** A1 / UC-015-R4

Given el perfil está vacío y la persona inicia su primera interacción con el asistente  
When comienza la conversación  
Then el sistema propone una entrevista para completar el perfil  
And permite que la persona la responda, la posponga o la omita  
And no exige completar la entrevista antes de continuar la conversación

### Scenario: Continuar sin completar la entrevista

**Ref:** A2 / UC-015-R4

Given el perfil está vacío o parcialmente completado y el sistema ha propuesto la entrevista  
When la persona decide omitirla  
Then el sistema continúa la conversación normalmente  
And no actualiza como confirmados los datos que la persona no aportó

### Scenario: Retomar una entrevista pospuesta

**Ref:** A3 / UC-015-R5

Given existe una entrevista pospuesta con datos del perfil todavía ausentes  
When la persona decide retomarla en una consulta posterior  
Then el sistema pregunta por los datos que siguen ausentes  
And no vuelve a exigir los datos que ya están confirmados

### Scenario: Actualizar varios datos de una misma consulta

**Ref:** A4 / UC-015-R6

Given existe una consulta en curso  
When la persona aporta varios datos personales y uno de ellos necesita aclaración  
Then el sistema actualiza al cerrar la consulta cada dato claro y confirmado  
And no descarta los demás datos admisibles porque uno necesite aclaración  
And el dato pendiente no aparece como confirmado

### Scenario: Conservar la última corrección de un dato

**Ref:** A5 / UC-015-R7; A6 / UC-015-R7  

Given la persona tiene un valor vigente para un dato del perfil  
When comunica y confirma un nuevo valor para ese mismo dato durante la consulta  
Then al cerrar la consulta el sistema conserva el nuevo valor como vigente  
And no conserva el valor anterior dentro del perfil  
And no crea un historial propio de ese dato

### Scenario: Mantener ausente un dato desconocido

**Ref:** A7 / UC-015-R8

Given la persona está completando o actualizando su perfil  
When declara que desconoce un dato  
Then el sistema mantiene ese dato ausente  
And no lo sustituye por una suposición  
And los demás datos confirmados pueden actualizarse al cerrar la consulta

### Scenario: Pedir aclaración sobre un dato ambiguo

**Ref:** A8 / UC-015-R8, RN-023

Given la persona comunica un dato ambiguo, contradictorio o que no puede confirmarse  
When el sistema intenta actualizar el perfil al cerrar la consulta  
Then pide una aclaración comprensible  
And mientras no recibe confirmación no sustituye el valor vigente anterior  
And no incorpora el dato ambiguo como si estuviera confirmado

### Scenario: Conservar el perfil cuando no se aporta ningún dato

**Ref:** A9

Given existe un perfil vacío o parcialmente completado  
When la consulta se cierra sin que la persona aporte ningún dato del perfil  
Then el sistema conserva el perfil exactamente como estaba

### Scenario: No convertir un hecho clínico en dato del perfil

**Ref:** A10 / UC-015-R9

Given existe una consulta en curso  
When la persona menciona un hecho clínico que no es un dato del perfil  
Then el sistema lo atiende por su cauce de información clínica  
And no lo incorpora al perfil por el solo hecho de haberlo mencionado

### Scenario: No incorporar un dato que no supera la comprobación

**Ref:** E1 / RN-018, RN-020

Given la persona ha aportado un dato que no supera la comprobación necesaria  
When la consulta se cierra  
Then el sistema no incorpora ese dato  
And conserva el valor vigente anterior si existía  
And permite continuar o volver a intentarlo

### Scenario: Mantener el perfil intacto si falla la actualización

**Ref:** E2 / UC-015-R10

Given la persona ha confirmado datos para actualizar su perfil  
When la actualización no puede completarse al cerrar la consulta  
Then el sistema informa de un fallo recuperable en español  
And conserva el perfil exactamente como estaba  
And no presenta la actualización como realizada  
And permite reintentarla

## Coverage notes

- **Happy path:** cubierto por `Actualizar el perfil al cerrar una consulta`.
- **Alternate flows:** `A1`, `A2`, `A3`, `A4`, `A5`, `A6`, `A7`, `A8`, `A9` y `A10` tienen un escenario
  cada uno; `A5` y `A6` comparten escenario porque ambos expresan la sustitución del valor vigente.
- **Exceptions:** `E1` y `E2` tienen un escenario cada uno.
- **Edge cases:** no se añade una categoría aparte. Los límites relevantes quedan cubiertos dentro de
  los escenarios existentes: perfil vacío (`A1`), entrevista omitida (`A2`), entrevista retomada (`A3`),
  corrección del último valor (`A5` y `A6`), dato desconocido (`A7`), dato ambiguo (`A8`) y consulta sin
  datos del perfil (`A9`).
- **Reglas sin escenario propio:** RN-002, RN-005, RN-006, RN-015, RN-016, RN-025, RN-029 y RN-030 son
  reglas transversales que se verifican en los escenarios de sus casos de uso de origen; RN-018 y RN-020
  se ejercitan mediante `E1` y `E2`.

## Traceability

Pruebas implementadas. Las rutas son relativas a la raíz del repositorio.

| Escenario | Prueba |
| --------- | ------ |
| Actualizar el perfil al cerrar una consulta | `backend/src/test/java/com/careme/backend/service/PatientProfileFlowTest.java` (`publishesConfirmedProfileValuesOnlyWhenTheConsultationCloses`) |
| Proponer la entrevista inicial sin hacerla obligatoria | `backend/.../FakeClinicalAgentTest.java` (`answersAGreetingWithoutAskingForAnyOperation`); `frontend/src/features/chat/components/ChatWorkspace.test.tsx` (`offers the profile interview without making it mandatory`) |
| Continuar sin completar la entrevista | `backend/.../FakeClinicalAgentTest.java` (`letsThePersonSkipTheOptionalProfileInterview`); `ChatWorkspace.test.tsx` (`sends a skip or a resume as an ordinary turn, without a new payload shape`) |
| Retomar una entrevista pospuesta | `backend/.../FakeClinicalAgentTest.java` (`resumesTheInterviewByAskingOnlyForTheFirstMissingField`, `handsTheCurrentProfileToTheAssistantSoTheInterviewCanResume`); `AgentToolContractTest.java` (`carriesOnlyTheMissingProfileFieldsToTheAssistant`) |
| Actualizar varios datos de una misma consulta | `backend/.../PatientProfileFlowTest.java` (`keepsProfileCandidatesSeparateFromClinicalNotesInOneConsultation`) |
| Conservar la última corrección de un dato | `backend/.../PatientProfileFlowTest.java` (`publishesOnlyTheLastConfirmedValueAndLeavesUnknownFieldsAbsent`) |
| Mantener ausente un dato desconocido | `backend/.../PatientProfileFlowTest.java` (`publishesOnlyTheLastConfirmedValueAndLeavesUnknownFieldsAbsent`); `PatientProfileMarkdownStoreTest.java` (`roundTripsConfirmedAndAbsentFields`) |
| Pedir aclaración sobre un dato ambiguo | `backend/.../AgentOperationExecutorTest.java` (rechazo de operaciones de perfil no confirmadas); `ChatWorkspace.test.tsx` (`asks for clarification about an ambiguous profile datum without exposing internals`) |
| Conservar el perfil cuando no se aporta ningún dato | `backend/.../PatientProfileMarkdownStoreTest.java` (`readsAnAbsentProfileAsEmpty`); `EncounterServiceTest.java` (`closesAConsultationWithNoFactsWithoutRegisteringAnything`) |
| No convertir un hecho clínico en dato del perfil | `backend/.../FakeClinicalAgentTest.java` (`keepsAClinicalFactOutOfTheProfileChannel`); `ChatWorkspace.test.tsx` (`keeps the profile, the events and the measurements apart in one turn`) |
| No incorporar un dato que no supera la comprobación | `backend/.../AgentOperationTest.java` (conjunto cerrado de operaciones); `PatientProfileFlowTest.java` (validación de candidatos) |
| Mantener el perfil intacto si falla la actualización | `backend/.../PatientProfileMarkdownStoreTest.java` (`keepsThePreviousSnapshotWhenPublicationFails`); `PatientProfileFlowTest.java` (`retriesProfilePublicationAfterARecoverableCloseFailure`); `ChatWorkspace.test.tsx` (`offers a retry when a profile update could not be completed`) |
| Entrevista omitida y retomada de extremo a extremo | `frontend/e2e/playwright/uc-015.spec.ts` (`P1`) |
| Corrección, dato desconocido y ausencia de actualización parcial de extremo a extremo | `frontend/e2e/playwright/uc-015.spec.ts` (`P2`) |

