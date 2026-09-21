package com.careme.backend.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.careme.backend.entity.Measurement;
import com.careme.backend.entity.MeasurementDraft;

/**
 * Data access contract for body measurements, the legacy daily view of weight and
 * abdominal circumference.
 *
 * <p>The only implementation composes it from the metric model; the service depends
 * on this interface, not on JPA.
 */
public interface MeasurementRepository {

    /**
     * @return every stored measurement, in no particular order
     */
    List<Measurement> findAll();

    /**
     * @return the measurement recorded on the given day, if the day has both metrics
     */
    Optional<Measurement> findByDate(LocalDate date);

    /**
     * Stores the weight and the abdominal circumference of a day. A day that already
     * had one keeps its identity and gets its values replaced, so no day ends up with
     * two measurements.
     */
    Measurement upsert(LocalDate date, BigDecimal weightKg, BigDecimal waistCm);

    /**
     * @return the days among the given ones that already have a measurement
     */
    Set<LocalDate> findExistingDates(Collection<LocalDate> dates);

    /**
     * Stores several measurements at once: a day that already had one keeps its
     * identity and gets its values replaced, and a day that had none gets a new
     * row. The whole batch is read with one query and flushed once, so importing
     * a large file does not cost a round trip per row.
     *
     * @return the stored measurements, in the order of the drafts
     */
    List<Measurement> upsertAll(List<MeasurementDraft> drafts);
}
