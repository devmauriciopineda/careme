package com.careme.backend.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * A question about the person's own measurements and the criteria used to retrieve
 * the measurements that can answer it.
 *
 * <p>The metrics travel as the codes the person named, not as resolved metrics: an
 * empty list means the question named none, and a code the catalogue does not admit
 * means the question named a metric outside the tracking. Resolving both cases is
 * the application's job, not the assistant's.
 *
 * @param question                 the question, in the person's own words
 * @param metricCodes              the metric codes the question named, possibly empty
 * @param fromDate                 the start of the questioned period, or {@code null}
 * @param toDate                   the end of the questioned period, or {@code null}
 * @param interpretationRequested  whether the question asked for an interpretation or
 *                                 a recommendation about the values
 */
public record MeasurementQueryIntent(
        String question,
        List<String> metricCodes,
        LocalDate fromDate,
        LocalDate toDate,
        boolean interpretationRequested) {

    public MeasurementQueryIntent {
        metricCodes = metricCodes == null ? List.of() : List.copyOf(metricCodes);
    }
}
