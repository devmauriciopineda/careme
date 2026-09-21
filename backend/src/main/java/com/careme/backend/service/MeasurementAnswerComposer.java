package com.careme.backend.service;

import java.util.List;

/**
 * Composes the Spanish answer to a question about the person's measurements.
 *
 * <p>The measurements arrive already formatted by the application. A composer
 * frames them; it never produces the numbers, because a rounded or converted value
 * would be an altered record.
 */
public interface MeasurementAnswerComposer {

    /**
     * States the clinical limit when the person asked for an interpretation or a
     * recommendation, without substituting one.
     */
    String DECLINED =
            "No puedo interpretar tus valores ni recomendarte nada sobre ellos:"
                    + " este asistente no diagnostica ni recomienda tratamiento."
                    + " Lo que sí puedo darte son los valores tal como están registrados.";

    /**
     * @param question                the question the person asked
     * @param facts                   the measurements retrieved, already formatted
     * @param interpretationRequested whether the person asked for an interpretation or
     *                                a recommendation about the values
     * @return the Spanish answer, in which every fact must appear verbatim
     */
    String compose(String question, List<MeasurementFact> facts, boolean interpretationRequested);
}
