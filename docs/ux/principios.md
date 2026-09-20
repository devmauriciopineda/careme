# Principios de UX de Careme

## 1. Propósito

Este documento define las reglas transversales que deben orientar la experiencia
de Careme. Sirve para tomar decisiones de diseño coherentes con la visión del
producto: ayudar a una persona a registrar, ordenar y consultar su historia
clínica personal sin convertir al asistente en una autoridad clínica.

## 2. Alcance

Estos principios aplican a las experiencias de conversación, registro,
consulta, recuperación, síntesis y revisión de información clínica personal.
Orientan decisiones sobre contenido, interacción, estados de incertidumbre,
errores y control del usuario. No sustituyen las especificaciones de flujos,
componentes visuales ni requisitos funcionales.

## 3. Principios

## UX-01 — Hablar con naturalidad

La persona debe poder expresar un hecho o una pregunta en lenguaje cotidiano,
sin conocer la estructura interna de Careme ni una terminología clínica
específica.

### Implicación

La experiencia debe priorizar la intención y el significado expresados por la
persona. Los detalles de clasificación y organización deben aparecer solo cuando
sean necesarios para confirmar, corregir o completar la información.

## UX-02 — Conservar la fidelidad del relato

Careme debe preservar lo que la persona dijo, incluyendo el alcance, el contexto
y la precisión temporal disponible, en lugar de convertirlo silenciosamente en
una versión más precisa o más concluyente.

### Implicación

Una fecha exacta, aproximada o desconocida debe comunicarse como tal. Cuando una
interpretación no sea segura, el producto debe mostrar la incertidumbre o pedir
una aclaración en vez de elegir una única lectura sin avisar.

## UX-03 — Aclarar antes de asumir

Cuando la intención, el hecho o la relación temporal sean ambiguos, Careme debe
pedir la mínima aclaración necesaria antes de registrar o responder como si
existiera certeza.

La aclaración debe ser concreta y permitir continuar desde el lenguaje de la
persona. No debe exigir que el usuario reformule todo su mensaje ni que conozca
el modelo de datos.

## UX-04 — Hacer visible la procedencia

La persona debe poder distinguir entre un hecho registrado, la fuente que lo
respalda y una síntesis generada por la IA.

### Implicación

Las respuestas que dependan de la historia personal deben facilitar la revisión
de los registros relevantes. Una síntesis no debe presentarse con más autoridad
que los hechos que la sustentan.

## UX-05 — Mantener el control en manos del usuario

Careme debe ayudar a organizar la historia de la persona sin apropiarse de su
significado ni ejecutar decisiones clínicas en su nombre.

### Implicación

El usuario debe poder reconocer qué se va a registrar, detectar una interpretación
incorrecta y corregirla. Las acciones que cambien o incorporen información deben
ser comprensibles y no depender de una inferencia oculta del asistente.

## UX-06 — Ser útil sin cruzar el límite clínico

La experiencia debe apoyar la memoria y la consulta de información personal, sin
simular diagnóstico, recomendación de tratamiento o sustitución de atención
médica.

El producto debe responder de forma clara cuando una petición exceda ese límite,
sin transformar una negativa de seguridad en una interpretación clínica del caso.

## UX-07 — Organizar alrededor del tiempo

La experiencia debe ayudar a reconstruir qué ocurrió, cuándo ocurrió y cómo se
relacionan los hechos a lo largo del tiempo, manteniendo visibles las fechas
aproximadas y los vacíos de información.

### Implicación

Las consultas, resúmenes y comparaciones deben conservar el orden temporal y no
mezclar hechos de distintos momentos de una manera que sugiera una evolución no
registrada.

## 4. Aplicación

Estos principios deben revisarse al diseñar una nueva experiencia y al evaluar
una existente. Para cada decisión relevante, el equipo debe comprobar qué
principio protege, qué información necesita entender el usuario y qué ocurre si
el dato es ambiguo, incompleto o no está respaldado.

La visión del producto y sus límites están definidos en
[docs/vision.md](../vision.md). El alcance funcional se mantiene en el
[roadmap del asistente](../roadmap/roadmap_asistente_historia_clinica.md).