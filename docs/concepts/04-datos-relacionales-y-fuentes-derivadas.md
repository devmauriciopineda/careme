# 04 — Persistencia: PostgreSQL, datos relacionales y búsqueda textual

Este documento explica cómo se conserva el estado de Careme y cómo se consultan
los hechos clínicos. Presenta el modelo relacional, las capacidades particulares
de PostgreSQL, la relación entre Spring Data JPA y Hibernate, y el índice de
búsqueda basado en `tsvector`, `tsquery` y `ts_rank`.

La persistencia tiene dos partes con funciones distintas: PostgreSQL almacena
datos estructurados y un índice derivado; el sistema de archivos conserva los
documentos Markdown que son la fuente de verdad de los hechos clínicos.

## Índice

1. [Qué es la persistencia](#1-qué-es-la-persistencia)
2. [Qué es una base de datos relacional](#2-qué-es-una-base-de-datos-relacional)
3. [PostgreSQL como motor de persistencia](#3-postgresql-como-motor-de-persistencia)
4. [Spring Data JPA, JPA e Hibernate](#4-spring-data-jpa-jpa-e-hibernate)
5. [Modelo relacional de Careme](#5-modelo-relacional-de-careme)
6. [Índices y restricciones](#6-índices-y-restricciones)
7. [Fuente de verdad y datos derivados](#7-fuente-de-verdad-y-datos-derivados)
8. [Búsqueda full-text de PostgreSQL](#8-búsqueda-full-text-de-postgresql)
9. [Otras estrategias de búsqueda](#9-otras-estrategias-de-búsqueda)
10. [Transacciones y coherencia](#10-transacciones-y-coherencia)
11. [Reconstrucción y operación del índice](#11-reconstrucción-y-operación-del-índice)

## 1. Qué es la persistencia

La **persistencia** es la conservación de datos más allá de la duración de una
petición o del proceso que los produjo. Una variable en memoria desaparece al
reiniciar la aplicación; una fila confirmada en PostgreSQL o un archivo escrito
en disco puede recuperarse después.

Un **sistema de persistencia** define dónde se guardan los datos, cómo se
identifican, qué reglas deben cumplir, cómo se consultan y qué sucede cuando
varias operaciones ocurren al mismo tiempo.

Careme usa dos medios:

- PostgreSQL 17 para mediciones y para un índice consultable de hechos clínicos.
- Archivos Markdown en `data/events/` para conservar los hechos clínicos como
  documentos legibles y canónicos.

La elección depende de la forma del dato. Las mediciones tienen columnas,
tipos, rangos y unicidad por fecha. Un hecho clínico conserva texto y metadatos
en un documento, por lo que su representación original es más importante que
su descomposición en muchas columnas.

```mermaid
flowchart LR
    App["Aplicación"] --> Rel[("PostgreSQL")]
    App --> Files["Markdown"]
    Rel --> Measurements["measurements<br/>fuente de verdad"]
    Rel --> Index["clinical_event_index<br/>índice derivado"]
    Files --> Events["data/events/*.md<br/>fuente de verdad"]
```

## 2. Qué es una base de datos relacional

Una **base de datos relacional** organiza datos en relaciones, que normalmente
se representan como tablas. Una tabla contiene **filas**, que representan
ocurrencias concretas, y **columnas**, que representan atributos con un tipo
determinado.

```text
Tabla measurements
┌────────────┬────────────┬───────────┬─────────┐
│ id         │ date       │ weight_kg │ waist_cm│  <- columnas
├────────────┼────────────┼───────────┼─────────┤
│ 7d...       │ 2026-09-18 │ 78.40     │ 92.10   │  <- fila
└────────────┴────────────┴───────────┴─────────┘
```

El término **relacional** no significa simplemente “datos en tablas”. Significa
que el modelo se apoya en relaciones, claves, restricciones y operaciones
formales sobre conjuntos de filas. Una consulta SQL puede combinar tablas,
filtrar relaciones y ordenar resultados sin que el programa tenga que recorrer
manualmente cada objeto.

### 2.1 Elementos fundamentales

Una **clave primaria** identifica de forma única cada fila. En `measurements`,
`id` es un UUID y no puede ser nulo ni repetirse.

Una **clave candidata** es cualquier conjunto mínimo de columnas que podría
identificar una fila. En Careme, la fecha es candidata para las mediciones porque
solo se permite una por día; se expresa mediante `UNIQUE (date)`.

Una **restricción** es una regla que el motor comprueba al insertar o modificar
datos. Ejemplos:

- `NOT NULL`: la columna debe tener un valor.
- `UNIQUE`: no puede existir el mismo valor o combinación dos veces.
- `CHECK`: una expresión debe ser verdadera.
- `PRIMARY KEY`: combina identificación única y no nulidad.
- `FOREIGN KEY`: exige que una referencia apunte a una fila existente.

Una **transacción** agrupa operaciones para que el motor las confirme o revierta
como una unidad. Sus garantías se describen con ACID: atomicidad, consistencia,
aislamiento y durabilidad.

### 2.2 Tipos y semántica

El tipo de una columna no es solo documentación: limita los valores y determina
cómo PostgreSQL los almacena y compara. Careme usa, entre otros:

| Tipo | Uso | Propiedad relevante |
| --- | --- | --- |
| `UUID` | Identificadores | Identidad sin depender de un contador visible |
| `DATE` | Día de una medición o hecho | No incluye hora ni zona horaria |
| `TIMESTAMPTZ` | Momento de creación | Representa un instante con normalización de zona |
| `NUMERIC(5, 2)` | Peso y cintura | Decimal exacto con precisión y escala |
| `VARCHAR` | Códigos y valores acotados | Texto con límite declarado |
| `TEXT` | Contenido clínico | Texto largo sin límite pequeño impuesto por la columna |
| `tsvector` | Índice lógico de texto | Lexemas normalizados con posiciones y pesos |

Usar `NUMERIC` para una medida decimal evita los errores de representación de
los tipos binarios de coma flotante. El esquema expresa así tanto el significado
como la precisión esperada del dato.

## 3. PostgreSQL como motor de persistencia

**PostgreSQL** es un sistema gestor de bases de datos relacional y objeto-
relacional. Además de tablas y SQL, ofrece tipos ricos, índices especializados,
funciones, extensiones y un modelo transaccional robusto.

### 3.1 Características relevantes

**Transacciones y MVCC.** PostgreSQL usa **MVCC** (Multi-Version Concurrency
Control). En lugar de hacer que toda lectura espere a cada escritura, conserva
versiones de las filas y permite que una transacción vea un estado coherente
según su aislamiento. Esto favorece la concurrencia entre lecturas y escrituras,
aunque no elimina la necesidad de restricciones y diseño correcto.

**Restricciones en el motor.** PostgreSQL ejecuta `UNIQUE`, `CHECK`, claves
primarias y otras reglas incluso si la escritura no viene de Careme. La
aplicación puede validar antes para producir mejores mensajes, pero la base de
datos es la última autoridad sobre sus invariantes.

**Índices especializados.** PostgreSQL incluye B-tree para igualdad, rangos y
orden; GIN para estructuras compuestas como documentos de texto; GiST y SP-GiST
para ciertas búsquedas espaciales y extensibles; y BRIN para datos grandes
correlacionados físicamente. Careme usa B-tree implícitos en claves y unicidad,
y GIN para el vector de búsqueda textual.

**Tipos y funciones de texto.** PostgreSQL incluye `TEXT`, expresiones regulares,
concatenación, normalización de búsqueda y operadores full-text. Esto permite que
la búsqueda clínica se ejecute cerca de los datos y dentro de las mismas
transacciones que actualizan el índice.

**Extensibilidad.** El motor puede incorporar tipos, funciones, operadores y
extensiones. Careme no necesita una base vectorial separada para su búsqueda
actual: utiliza las capacidades full-text incluidas en PostgreSQL.

### 3.2 Migraciones con Flyway

Una **migración** es un cambio versionado del esquema. Flyway registra las
migraciones aplicadas y ejecuta las pendientes en orden. En Careme:

- `V1__create_measurements_table.sql` crea la tabla de mediciones y sus reglas.
- `V2__create_clinical_event_index.sql` crea el índice derivado y su `tsvector`.
- Hibernate usa `ddl-auto: validate`, por lo que comprueba el esquema pero no lo
  crea ni lo modifica.

Esta división hace explícito quién posee cada cambio: Flyway transforma la base
de datos; Hibernate verifica que el modelo de persistencia sea compatible.

## 4. Spring Data JPA, JPA e Hibernate

Estas tres piezas están relacionadas, pero no son la misma cosa.

**JPA** (Jakarta Persistence) es una especificación. Define conceptos y APIs
para mapear objetos Java a tablas, identificar entidades, ejecutar consultas y
coordinar un contexto de persistencia. JPA describe el contrato, no una
implementación concreta.

**Hibernate ORM** es la implementación JPA utilizada por el backend. Convierte
entidades Java en SQL, mantiene el contexto de persistencia, detecta cambios y
coordina el `flush` con la transacción. Hibernate pertenece al backend y es su
motor ORM; no es una característica propia de PostgreSQL.

**Spring Data JPA** es una capa de Spring que simplifica la declaración y el uso
de repositorios JPA. Puede generar consultas a partir de nombres de métodos,
proporcionar operaciones CRUD y conectar repositorios con el contenedor de
transacciones. No reemplaza a Hibernate: normalmente delega en un proveedor JPA
como Hibernate.

La relación puede representarse así:

```mermaid
flowchart TB
    Service["Servicio de aplicación"] --> Repo["Repositorio Spring"]
    Repo --> SD["Spring Data JPA"]
    SD --> JPA["API JPA"]
    JPA --> Hibernate["Hibernate ORM"]
    Hibernate --> JDBC["JDBC + SQL"]
    JDBC --> PG[("PostgreSQL")]
```

### 4.1 Ejemplo en Careme

`MeasurementJpaDao` extiende `JpaRepository<MeasurementEntity, UUID>`. Spring
Data JPA proporciona operaciones como `findAll`, `findById` y `save`; además,
deriva una consulta para `findByDate` y otra para `findByDateIn`.

`MeasurementJpaRepository` es otra capa: traduce `MeasurementEntity` al tipo de
dominio `Measurement`, declara los límites transaccionales y expone el contrato
que necesita el servicio. Así, el dominio no depende directamente de JPA.

```java
public interface MeasurementJpaDao
        extends JpaRepository<MeasurementEntity, UUID> {
    Optional<MeasurementEntity> findByDate(LocalDate date);
    List<MeasurementEntity> findByDateIn(Collection<LocalDate> dates);
}
```

### 4.2 Contexto de persistencia y entidades

El **contexto de persistencia** es el conjunto de entidades que Hibernate está
siguiendo durante una unidad de trabajo. Para una identidad de base de datos,
mantiene una instancia administrada y puede detectar que sus propiedades
cambiaron.

Una entidad JPA no es necesariamente el modelo de dominio ni el DTO HTTP. En
Careme se distinguen:

```text
MeasurementRequest -> entrada JSON
Measurement         -> dominio
MeasurementEntity   -> entidad JPA
MeasurementResponse -> salida JSON
```

Esta separación evita que una anotación de persistencia defina por accidente el
contrato público o las reglas del dominio.

### 4.3 Por qué la búsqueda clínica usa JDBC

La persistencia de mediciones usa Spring Data JPA porque trabaja con entidades y
operaciones CRUD relacionales. La consulta full-text de hechos clínicos usa
`JdbcTemplate` en `ClinicalEventQueryRepository` porque necesita expresar
explícitamente operadores y funciones PostgreSQL como `@@`, `to_tsquery` y
`ts_rank`.

Esto no contradice el uso de Hibernate. Son dos adaptadores de persistencia para
necesidades distintas:

| Necesidad | Adaptador actual |
| --- | --- |
| CRUD y consultas derivadas de mediciones | Spring Data JPA + Hibernate |
| Búsqueda PostgreSQL full-text sobre índice derivado | `JdbcTemplate` + SQL parametrizado |
| Fuente canónica de hechos clínicos | Adaptador del sistema de archivos |

## 5. Modelo relacional de Careme

La tabla de mediciones es una fuente de verdad relacional:

```sql
CREATE TABLE measurements (
    id        UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    date      DATE          NOT NULL,
    weight_kg NUMERIC(5, 2) NOT NULL,
    waist_cm  NUMERIC(5, 2) NOT NULL,
    CONSTRAINT uq_measurements_date UNIQUE (date),
    CONSTRAINT ck_measurements_weight_kg_positive CHECK (weight_kg > 0),
    CONSTRAINT ck_measurements_waist_cm_positive CHECK (waist_cm > 0)
);
```

Cada decisión tiene una consecuencia:

- `UNIQUE (date)` hace que una medición por día sea una propiedad del dato.
- `NUMERIC(5, 2)` almacena un decimal exacto con dos posiciones decimales.
- `CHECK` impide pesos y perímetros no positivos.
- `PRIMARY KEY` proporciona identidad estable e índice único.

Los hechos clínicos tienen otra forma: `clinical_event_index` contiene `id`,
`code`, `type`, fechas, contenido y metadatos suficientes para consultar, pero no
es la fuente original del texto. Los documentos Markdown conservan esa fuente.

## 6. Índices y restricciones

Un **índice** es una estructura auxiliar que evita leer toda la tabla para una
consulta frecuente. El coste de lectura disminuye, pero cada escritura debe
actualizar el índice y el índice ocupa espacio adicional.

### 6.1 B-tree

Un **B-tree** mantiene claves ordenadas en un árbol equilibrado. Es apropiado
para igualdad, rangos y ordenación:

```sql
SELECT *
FROM measurements
WHERE date BETWEEN DATE '2026-09-01' AND DATE '2026-09-18'
ORDER BY date;
```

La clave primaria y la restricción `UNIQUE (date)` crean índices que permiten
localizar y ordenar mediciones eficientemente. La complejidad exacta depende del
planificador y de la distribución de datos, pero conceptualmente se evita un
recorrido completo de la tabla.

### 6.2 GIN

**GIN** (Generalized Inverted Index) es un índice para valores que contienen
múltiples elementos indexables. En full-text, esos elementos son los lexemas de
un `tsvector`. El índice relaciona cada lexema con las filas que lo contienen.

```sql
CREATE INDEX idx_clinical_event_index_search_vector
    ON clinical_event_index USING GIN (search_vector);
```

GIN acelera la identificación de candidatos, pero la consulta todavía puede
necesitar filtros, cálculo de relevancia y ordenación posterior.

## 7. Fuente de verdad y datos derivados

Una **fuente de verdad** es el almacenamiento canónico de un dato. Un **dato
derivado** es una representación calculada que puede reconstruirse desde la
fuente.

En Careme:

```mermaid
flowchart LR
    MD["Markdown clínico<br/>fuente de verdad"] -->|indexación| IDX["clinical_event_index<br/>dato derivado"]
    IDX -->|búsqueda| Query["Consulta full-text"]
    MD -->|reconstrucción| IDX
```

El índice PostgreSQL no inventa el texto clínico. Se rellena a partir de los
documentos y puede borrarse y reconstruirse sin perder la fuente original.

El sistema de archivos y PostgreSQL no comparten una transacción distribuida. La
escritura compuesta sigue una estrategia de compensación: se escriben los
Markdown, se actualiza el índice y, si la segunda parte falla, se eliminan los
documentos recién creados. Una **compensación** es una operación posterior que
revierte el efecto de una operación anterior cuando no existe una transacción
única que abarque todos los sistemas.

## 8. Búsqueda full-text de PostgreSQL

La **búsqueda full-text** no compara simplemente cadenas completas. Convierte el
texto en unidades normalizadas, construye una expresión de consulta y busca
coincidencias mediante operadores especializados.

En Careme, la columna se genera así:

```sql
search_vector tsvector GENERATED ALWAYS AS (
    to_tsvector(
        'simple',
        coalesce(code, '') || ' ' || coalesce(type, '') || ' '
        || coalesce(date_text, '') || ' ' || content
    )
) STORED
```

`GENERATED ALWAYS AS ... STORED` hace que PostgreSQL calcule y almacene el
vector cada vez que cambia el texto relevante. La aplicación no puede actualizar
el vector de forma independiente y dejarlo desincronizado.

### 8.1 Qué es `tsvector`

Un **`tsvector`** es la representación normalizada de un documento para
búsqueda. Contiene **lexemas**, no el texto original. Cada lexema puede conservar
posiciones dentro del documento y un peso de relevancia.

Por ejemplo, conceptualmente:

```text
Texto:  "dolor abdominal registrado"
tsvector: 'abdominal':2 'dolor':1 'registrado':3
```

La posición permite evaluar proximidad y relevancia. La transformación depende
de la configuración lingüística. La configuración `simple` usada por Careme
separa y normaliza de forma básica, pero no aplica stemming específico del
español ni elimina un conjunto de palabras vacías del español. Esto favorece
previsibilidad cuando el texto mezcla idiomas y conserva términos cercanos a la
forma escrita por la persona.

`tsvector` no es un índice físico por sí mismo. Es un valor lógico que después
puede acelerarse con un índice GIN.

### 8.2 Qué es `tsquery`

Un **`tsquery`** es una expresión de búsqueda que describe qué lexemas deben
coincidir. No contiene necesariamente el texto original de la pregunta: contiene
condiciones sobre el vector.

Sus operadores principales son:

| Operador | Significado |
| --- | --- |
| `&` | AND: ambas condiciones deben coincidir |
| `|` | OR: basta una de las condiciones |
| `!` | NOT: excluye una condición |
| `<->` | Proximidad: términos adyacentes en el orden indicado |
| `:*` | Prefijo, cuando se construye una consulta que lo permite |

Ejemplos conceptuales:

```sql
-- Deben aparecer ambos lexemas.
'tdolor' & 'abdominal'

-- Debe aparecer uno de los dos.
'tos' | 'fiebre'

-- Buscar un término cerca de otro.
'tdolor' <-> 'abdominal'
```

Careme recibe términos ya estructurados por su capa de interpretación y los
convierte en una cadena segura: las palabras dentro de un término se unen con
`&`, y términos alternativos se unen con `|`. Después PostgreSQL interpreta la
cadena con `to_tsquery('simple', ?)`. La consulta se parametriza mediante
`JdbcTemplate`; los valores no se concatenan directamente al SQL.

El operador `@@` aplica la consulta al vector:

```sql
WHERE search_vector @@ to_tsquery('simple', ?)
```

La expresión es verdadera cuando el `tsvector` satisface el `tsquery`.

### 8.3 Qué es `ts_rank`

**`ts_rank`** calcula una puntuación de relevancia para una coincidencia entre
un `tsvector` y un `tsquery`. No decide si una fila coincide; esa decisión la
hace `@@`. `ts_rank` ordena las filas que ya coincidieron.

```sql
SELECT content,
       ts_rank(search_vector, to_tsquery('simple', ?)) AS rank
FROM clinical_event_index
WHERE search_vector @@ to_tsquery('simple', ?)
ORDER BY rank DESC, event_date DESC NULLS LAST;
```

La puntuación considera la frecuencia y posiciones de los lexemas, junto con la
configuración de pesos cuando se define. No equivale a una comprensión semántica
ni a una probabilidad de que el resultado responda correctamente a una pregunta.
Es una medida léxica de coincidencia.

PostgreSQL también ofrece `ts_rank_cd`, que incorpora cobertura y proximidad de
las coincidencias. Careme usa `ts_rank`, seguido de un desempate por fecha. Por
eso una fila puede tener alta puntuación porque comparte términos relevantes,
pero no necesariamente porque exprese el mismo significado que la pregunta.

### 8.4 Flujo completo

```mermaid
flowchart TB
    Text["Texto y metadatos del evento"] --> Norm["to_tsvector('simple', ...)" ]
    Norm --> Vector["search_vector tsvector"]
    Vector --> GIN["Índice GIN"]
    Terms["Términos de consulta"] --> Query["to_tsquery('simple', ...)" ]
    Query --> Match["@@"]
    GIN --> Match
    Vector --> Rank["ts_rank"]
    Query --> Rank
    Match --> Rank
    Rank --> Result["Resultados ordenados y limitados"]
```

La consulta actual combina búsqueda textual con filtros de tipo y fecha. Si no
hay coincidencias textuales y existe un filtro de metadatos, ejecuta una segunda
consulta solo por esos metadatos. El límite de resultados protege el tamaño de
la respuesta, pero significa que una búsqueda limitada no prueba la ausencia de
hechos.

## 9. Otras estrategias de búsqueda

La búsqueda full-text de PostgreSQL es una estrategia **léxica**: relaciona
formas normalizadas de palabras. Existen otras estrategias, con propiedades
diferentes.

| Estrategia | Qué compara | Ventaja | Limitación |
| --- | --- | --- | --- |
| `LIKE` / `ILIKE` | Subcadenas | Simple y útil para pocos datos | Puede recorrer muchas filas; poca relevancia |
| Trigramas (`pg_trgm`) | Fragmentos de tres caracteres | Tolerancia a errores y similitud textual | No representa bien el significado completo |
| Full-text | Lexemas y operadores | Integrado en PostgreSQL; rápido con GIN | Principalmente léxico; depende de configuración lingüística |
| BM25 | Frecuencia y rareza de términos en una colección | Ranking fuerte para recuperación textual | Requiere un motor o extensión que lo implemente |
| Embeddings | Vectores semánticos generados por un modelo | Encuentra significado y paráfrasis | Coste de modelo, almacenamiento, actualización y evaluación |
| Búsqueda híbrida | Léxica + semántica | Combina coincidencia exacta y significado | Más compleja de calibrar y explicar |

### 9.1 BM25

**BM25** es una función de ranking de recuperación de información. Considera,
entre otros factores, cuántas veces aparece un término en un documento, qué tan
raro es en toda la colección y cuánto mide el documento. La rareza se expresa
con una idea de frecuencia inversa de documento: un término común distingue
menos que uno poco frecuente.

BM25 no es lo mismo que `ts_rank`. Ambos producen un ranking léxico, pero usan
modelos diferentes. PostgreSQL proporciona full-text con `ts_rank`; no se debe
describir el comportamiento actual como BM25.

### 9.2 Embeddings y búsqueda vectorial

Un **embedding** es un vector numérico que representa rasgos semánticos de un
texto producido por un modelo. Dos textos con palabras diferentes pueden quedar
cerca si el modelo considera que expresan ideas relacionadas.

La **búsqueda vectorial** compara el vector de una consulta con los vectores de
los documentos mediante una distancia, como coseno o distancia euclídea. En el
ecosistema PostgreSQL suele apoyarse en `pgvector`, aunque esa extensión no forma
parte de la implementación actual de Careme.

Los embeddings son útiles cuando las personas formulan la misma idea con
vocabulario diferente, pero introducen decisiones adicionales: modelo de
embeddings, versión, dimensiones, indexación aproximada, umbral de similitud,
privacidad y estrategia de actualización.

### 9.3 Por qué Careme usa full-text aquí

La búsqueda actual necesita ser local, explicable y reconstruible desde los
documentos. `tsvector` conserva una relación visible entre términos y hechos;
GIN evita escanear toda la tabla; `tsquery` expresa condiciones controladas; y
`ts_rank` permite ordenar coincidencias sin introducir un modelo externo.

Eso no convierte full-text en la mejor estrategia universal. Significa que es la
estrategia adecuada para el contrato actual y que una eventual búsqueda híbrida
debería añadirse como una decisión explícita, con sus propios datos derivados y
criterios de evaluación.

## 10. Transacciones y coherencia

Una **transacción** es una unidad de trabajo que PostgreSQL confirma o revierte
como conjunto. En una escritura de mediciones, `saveAllAndFlush` envía el lote
y la transacción permite que un fallo revierta la operación completa.

`flush` y `commit` son distintos:

```text
BEGIN
  modificar entidades
  FLUSH    -- enviar cambios pendientes al motor
  COMMIT   -- hacerlos definitivos
```

Un `flush` exitoso todavía puede ser seguido por un rollback. El contexto de
persistencia de Hibernate coordina el estado de las entidades con el flush,
mientras PostgreSQL aplica la transacción y sus restricciones.

Las operaciones sobre Markdown y PostgreSQL no caben en la misma transacción.
La coherencia entre ambos se consigue con orden, detección de fallos y
compensación. El documento Markdown sigue siendo la fuente primaria; el índice
puede reconstruirse.

## 11. Reconstrucción y operación del índice

La reconstrucción lee los documentos clínicos, recalcula sus filas y vuelve a
crear el índice derivado. Es una operación **idempotente**: ejecutarla dos veces
produce el mismo estado lógico que ejecutarla una vez, suponiendo que la fuente
no cambió entre ambas ejecuciones.

El flujo conceptual es:

```text
1. leer todos los documentos Markdown
2. parsear front matter y contenido
3. validar tipos, fechas y códigos
4. limpiar o reemplazar el índice derivado
5. insertar filas; PostgreSQL genera search_vector
6. consultar mediante GIN + tsquery + ts_rank
```

Esta estrategia separa recuperación de datos y optimización de consultas. Si se
pierde `clinical_event_index`, no se pierde el texto clínico: se pierde una
representación derivada que puede volver a producirse.

La operación también debe distinguir dos preguntas diferentes:

- una búsqueda limitada responde si existen coincidencias entre sus filtros y su
  límite;
- un `COUNT(*)` sobre el alcance completo permite razonar sobre ausencia total,
  ausencia por tipo o ausencia por periodo.

La diferencia importa porque un índice de búsqueda prueba presencia de candidatos,
pero un resultado vacío no demuestra por sí solo que la historia carezca de
hechos.
