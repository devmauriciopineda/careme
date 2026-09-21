## Purpose

Mantener el catálogo de métricas que el sistema admite —qué mide cada una, con qué unidad de referencia se
guarda y qué equivalencias existen entre sus unidades— y ampliarlo solo cuando la persona confirma que
quiere empezar a registrar una métrica nueva.

## ADDED Requirements

### Requirement: Admitir un catálogo extensible de métricas

El sistema MUST admitir un catálogo de métricas que determine, para cada una, qué se mide y con qué unidad
de referencia se guarda. El catálogo MUST incluir al menos peso (kg), circunferencia abdominal (cm),
presión arterial (mmHg) y colesterol (mg/dL), y MUST poder ampliarse con métricas nuevas —creatinina,
glucosa en ayunas, triglicéridos u otras— sin cambiar cómo se registra ni cómo se consulta una medición.
Una métrica del catálogo MUST NOT sustituirse por otra métrica.

#### Scenario: Registrar contra el catálogo vigente

- **WHEN** una medición menciona una métrica que el catálogo admite
- **THEN** el sistema la trata con la unidad de referencia de esa métrica
- **AND** registra la medición con esa unidad

#### Scenario: Ampliar el catálogo sin alterar lo existente

- **WHEN** se incorpora una métrica nueva al catálogo
- **THEN** el registro y la consulta de las métricas que ya existían no cambian
- **AND** las mediciones ya guardadas conservan su métrica, su valor y su unidad

#### Scenario: No sustituir una métrica por otra

- **WHEN** una medición menciona una métrica
- **THEN** el sistema no la guarda como si fuera otra métrica del catálogo
- **AND** no la completa con el valor de otra métrica

### Requirement: Guardar toda medición en la unidad de referencia de su métrica

Cada métrica MUST tener una unidad de referencia, y toda medición MUST guardarse en ella con su valor
numérico. Una medición MUST NOT guardarse sin unidad.

#### Scenario: Guardar con la unidad de referencia

- **WHEN** una medición menciona una métrica del catálogo
- **THEN** el sistema guarda su valor en la unidad de referencia de esa métrica
- **AND** no guarda la medición sin unidad

### Requirement: Convertir un valor expresado en otra unidad de la misma métrica

Cuando la persona exprese el valor en otra unidad de la misma métrica, el sistema MUST convertirlo a la
unidad de referencia antes de guardarlo, sin alterarlo más allá del cambio de unidad. La equivalencia
entre las unidades de una métrica MUST ser única y MUST NOT depender del criterio del asistente.

#### Scenario: Convertir a la unidad de referencia

- **WHEN** la persona menciona una medición con un valor expresado en otra unidad de esa misma métrica
- **THEN** el sistema convierte el valor a la unidad de referencia de la métrica
- **AND** guarda la medición con la unidad de referencia
- **AND** el valor guardado no se altera más allá del cambio de unidad

#### Scenario: Convertir siempre con la misma equivalencia

- **WHEN** la misma cantidad se expresa más de una vez en otra unidad de la misma métrica
- **THEN** el sistema aplica siempre la misma equivalencia
- **AND** la equivalencia no depende de la métrica de la que venga el valor

### Requirement: Resolver la unidad de una medición solo cuando es inequívoca

El sistema MUST NOT suponer la unidad de una medición. Cuando la persona no la indique y del valor y de la
métrica no pueda deducirse una única unidad, el sistema MUST pedir que la concrete y MUST NOT registrar la
medición mientras siga sin estar clara. Cuando del valor y de la métrica se deduzca una única unidad
posible, el sistema MUST registrar la medición con esa unidad sin pedir una aclaración innecesaria.

#### Scenario: Pedir que se concrete la unidad

- **WHEN** la persona menciona una medición sin indicar la unidad
- **AND** del valor y de la métrica no puede deducirse la unidad con seguridad
- **THEN** el sistema no supone la unidad
- **AND** pide que la concrete
- **AND** si no la obtiene, no registra la medición

#### Scenario: Registrar sin pedir aclaración cuando la unidad es inequívoca

- **WHEN** la persona menciona una medición sin indicar la unidad
- **AND** del valor y de la métrica se deduce una única unidad posible
- **THEN** el sistema registra la medición con esa unidad
- **AND** no pide una aclaración innecesaria

### Requirement: Tratar una métrica compuesta como una sola medición

Una métrica compuesta MUST registrarse como una sola medición con sus valores, no como una medición por
valor. La presión arterial MUST tratarse como una métrica compuesta de dos valores, sistólica y
diastólica.

#### Scenario: Registrar una presión arterial

- **WHEN** la persona menciona su presión arterial con sus dos valores
- **THEN** el sistema la trata como una sola medición de la métrica compuesta
- **AND** guarda sus dos valores, sistólica y diastólica
- **AND** no la registra como dos mediciones distintas

#### Scenario: No separar los valores de una métrica compuesta

- **WHEN** una métrica compuesta se incorpora al seguimiento
- **THEN** el sistema la incorpora como una sola medición
- **AND** no crea una medición independiente por cada valor

### Requirement: Ampliar el catálogo solo con la confirmación de la persona

Cuando la persona mencione una métrica que el sistema todavía no admite, el sistema MUST informarle de que
esa medición no está quedando registrada y MUST preguntarle si quiere empezar a registrar esa métrica. La
métrica MUST pasar a admitirse únicamente si la persona lo confirma, y solo entonces MUST registrarse la
medición con ella. Sin esa confirmación, la métrica MUST NOT quedar admitida, la medición MUST NOT
incorporarse al seguimiento y MUST NOT sustituirse por otra métrica.

#### Scenario: Informar de una métrica todavía no admitida

- **WHEN** la persona menciona una métrica que el sistema todavía no admite
- **THEN** el sistema le informa de que esa medición no está quedando registrada
- **AND** le pregunta si quiere empezar a registrar esa métrica
- **AND** no registra la medición mientras no tenga su confirmación

#### Scenario: Ampliar el catálogo con la confirmación

- **WHEN** la persona confirma que quiere empezar a registrar la métrica que no estaba admitida
- **THEN** la métrica pasa a admitirse
- **AND** la medición se registra con ella

#### Scenario: No ampliar el catálogo sin confirmación

- **WHEN** la persona no confirma que quiere empezar a registrar esa métrica
- **THEN** la métrica no queda admitida
- **AND** esa medición no se incorpora al seguimiento
- **AND** no aparece en el seguimiento por sí sola
