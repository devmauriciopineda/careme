# Reglas de negocio transversales

Reglas que rigen todo el producto y que ningún caso de uso puede contradecir. Cada caso de uso
referencia en su sección 8 los identificadores `RN-` que le aplican y conserva aparte, numeradas
`UC-XXX-Rn`, las reglas que le son propias.

## Convenciones

- Una regla transversal tiene un identificador estable `RN-NNN` y no se renumera: se deroga y se
  añade otra.
- Un caso de uso **no puede contradecir** una regla transversal. Si necesita un matiz, lo declara
  como regla propia de su sección 8 y lo justifica.
- El texto de la regla se enuncia una sola vez, aquí; los casos de uso la referencian, no la copian.
- Los criterios de aceptación referencian estas reglas por identificador en su campo `Ref`.
- La columna **Alcance** señala los casos de uso a los que la regla aplica; una regla puede alcanzar
  casos de uso todavía previstos, y en ese caso se indica.

## 1. Fundamentación

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-001 | El sistema no presenta como hecho nada que no esté respaldado por un registro de la historia. | UC-004, UC-007, UC-008, UC-010, UC-012 | `mvp_alcance` §10.1; UC-007 §8 |
| RN-002 | Las respuestas sobre la historia se elaboran con los hechos recuperados y solo con ellos; lo que falta no se completa con conocimiento general, suposiciones ni inferencias. | UC-007, UC-008, UC-012 | `mvp_alcance` §10.3, §10.4; UC-007 §8 |
| RN-003 | Toda respuesta sobre la historia permite identificar los hechos en que se apoya. | UC-007, UC-012 | UC-007 §8 |
| RN-004 | El sistema no deduce hechos no registrados a partir de hechos sí registrados. | UC-007, UC-008, UC-012 | UC-008 §8 |

## 2. Temporalidad

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-005 | Toda fecha registrada tiene un grado de precisión: exacta, aproximada o desconocida. | UC-004, UC-011, UC-012 | UC-004 §8 |
| RN-006 | La precisión temporal de un registro nunca aumenta: una fecha aproximada no se presenta como exacta y una fecha desconocida no se sustituye por una suposición. | UC-004, UC-007, UC-011, UC-012 | UC-004 §8; UC-007 §8; UC-011 §8 |
| RN-007 | Junto con la fecha se conserva la expresión temporal que la persona usó, cuando la hubo. | UC-004, UC-011, UC-012 | UC-004 §8 |
| RN-008 | Las expresiones temporales relativas se interpretan tomando como referencia la fecha del día en que se atiende el mensaje. | UC-004, UC-012 | UC-004 §8; `mvp_alcance` §8 |
| RN-009 | Una fecha se interpreta y se muestra como un día del calendario, sin hora, y no se desplaza por la ubicación de la persona. | UC-001, UC-002, UC-003 | UC-001 §8; UC-002 §8, §9; UC-003 §8, §9 |

## 3. Ausencia de información

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-010 | La ausencia de registros se declara siempre de forma explícita; nunca se responde al vacío con silencio ni con una respuesta aparente. | UC-007, UC-008, UC-012 | UC-008 §8 |
| RN-011 | La ausencia se refiere a lo que consta en la historia, no a lo que haya ocurrido: la respuesta no afirma ni niega que el hecho haya sucedido. | UC-008, UC-012 | UC-008 §8, §9 |
| RN-012 | Una operación que no pudo completarse no es una ausencia de registros: son situaciones distintas y se comunican de forma distinta. | UC-007, UC-008, UC-011, UC-012 | UC-008 §8 |
| RN-013 | Ante la insistencia de la persona, la declaración se mantiene sin cambios; no se fabrica información para satisfacer la petición. | UC-008, UC-012 | UC-008 §8 |

## 4. Límite clínico

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-014 | La relación entre la persona y el asistente es de registro y consulta de su historia; no es una relación clínica. | UC-010, UC-012 | UC-010 §8 |
| RN-015 | El asistente no diagnostica, no recomienda tratamiento ni interpreta los datos de la persona. | UC-007, UC-010, UC-012 | UC-010 §8; UC-007 §10 |
| RN-016 | Ante una petición de diagnóstico, recomendación o interpretación, el asistente la declina de forma explícita y comprensible y no la sustituye por una respuesta inventada. | UC-010, UC-012 | UC-010 §8 |
| RN-017 | Los conceptos generales se explican en términos generales, sin aplicarlos al caso de la persona. | UC-010, UC-012 | UC-010 §8 |

## 5. Escritura, validación y no modificación

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-018 | Ningún hecho se incorpora a la historia sin una comprobación previa; una propuesta no comprobada no produce escritura. | UC-004, UC-012 | `mvp_alcance` §8 |
| RN-019 | Registrar no modifica lo ya registrado, salvo el reemplazo previsto para una medición del mismo día. | UC-002, UC-003 | UC-002 §8; UC-003 §8 |
| RN-020 | Una operación de escritura es de todo o nada: o queda completa, o la historia permanece exactamente como estaba. | UC-003, UC-004, UC-012 | UC-003 §8; UC-004 §7 |
| RN-021 | No se registran creencias, sospechas ni inferencias como si fueran hechos. | UC-004, UC-012 | `mvp_alcance` §10.2; UC-004 §8 |
| RN-022 | Volver a registrar un hecho ya registrado no crea un duplicado. | UC-004, UC-012 | UC-004 §8 |
| RN-023 | Cuando falta información para completar una operación, el sistema pide aclaración y no ejecuta ni inventa la operación. | UC-004, UC-007, UC-010, UC-012 | UC-004 A5; UC-007 E2; UC-010 A7 |
| RN-024 | Un hecho registrado queda disponible para consulta de inmediato. | UC-004, UC-011, UC-012 | UC-004 §8, §9 |
| RN-025 | Las operaciones de consulta nunca modifican la historia. | UC-007, UC-008, UC-010, UC-011, UC-012 | UC-007 §8; UC-008 §8; UC-010 §8; UC-011 §8 |

## 6. Conversación y cauce

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-026 | Cada mensaje se atiende por el cauce que le corresponde: consultar la historia, registrar un hecho, declarar la ausencia o conversar. | UC-004, UC-007, UC-008, UC-010, UC-012 | UC-004 A7; UC-007 §8; UC-010 §8 |
| RN-027 | Una pregunta que depende de la historia se atiende como consulta de la historia aunque esté en lenguaje coloquial; una que no depende no activa ninguna búsqueda. | UC-007, UC-010, UC-012 | UC-007 §8; UC-010 §8 |
| RN-028 | Un mensaje que mezcla asuntos generales con información propia se atiende por partes, cada una por el cauce que le corresponde. | UC-010, UC-012 | UC-010 §8 |
| RN-029 | La respuesta no expone detalles internos del sistema ni describe cómo se decide el cauce. | UC-004, UC-008, UC-010, UC-012 | UC-004 §8; UC-008 §8; UC-010 §8 |

## 7. Procedencia y fuente

| ID | Regla | Alcance | Origen |
| --- | --- | --- | --- |
| RN-030 | Toda la información pertenece a la historia de la propia persona; la fuente de lo que ella aporta es el paciente. | UC-004, UC-007, UC-011, UC-012 | UC-004 §8; UC-007 §8 |
| RN-031 | Todo hecho registrado declara de dónde procede: quién lo aportó, cuándo se registró y de qué consulta procede. | UC-013b | UC-013b §8; `roadmap` §4.1, §4.9 |
| RN-032 | La información derivada se marca como tal y se reconstruye desde los hechos; nunca es fuente de verdad. | UC-013b, UC-024 | UC-013b §8; `roadmap` §3.2, §4.10 |

## Trazabilidad

- Reglas derivadas de las reglas de comportamiento del alcance cerrado del MVP:
  `mvp_alcance_asistente_historia_clinica.md` §10.
- Reglas derivadas de casos de uso ya redactados: `UC-001`, `UC-002`, `UC-003`, `UC-004`, `UC-007`,
  `UC-008`, `UC-010`, `UC-011` y `UC-013b`, cuya sección 8 pasó a referenciar este catálogo.
- Reglas previstas para subfases posteriores: `RN-032` para `UC-024`
  (`roadmap_asistente_historia_clinica.md` §3.2, §4.10).
