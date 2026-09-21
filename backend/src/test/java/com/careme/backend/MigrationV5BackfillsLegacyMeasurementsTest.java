package com.careme.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Verifies the data migration of `V5__create_metric_measurements.sql` on a real
 * database: the schema is brought up to the legacy shape, filled with the old
 * day-by-day rows, and then migrated, so every day must survive as one measurement
 * per metric and the legacy table must be gone.
 *
 * <p>It runs against its own database inside the shared test container: the
 * container's default database is already migrated to the latest version by the
 * other Spring tests, and this one has to control how far Flyway goes before the
 * metric migration runs.
 */
class MigrationV5BackfillsLegacyMeasurementsTest {

    private static final String DATABASE = "careme_migration";

    private static String jdbcUrl;

    @BeforeAll
    static void migrateLegacyDataIntoTheMetricModel() throws SQLException {
        // Touching the shared container starts the one container every test uses.
        String containerUrl = PostgresIntegrationTest.POSTGRES.getJdbcUrl();
        String containerDatabase = PostgresIntegrationTest.POSTGRES.getDatabaseName();
        jdbcUrl = containerUrl.replace("/" + containerDatabase, "/" + DATABASE);

        try (Connection connection = DriverManager.getConnection(
                        containerUrl,
                        PostgresIntegrationTest.POSTGRES.getUsername(),
                        PostgresIntegrationTest.POSTGRES.getPassword());
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE DATABASE " + DATABASE);
        }

        // Up to V4: the schema still has the legacy daily table.
        flyway("4").migrate();

        try (Connection connection = open();
                Statement statement = connection.createStatement()) {
            statement.execute("""
                    INSERT INTO measurements (date, weight_kg, waist_cm) VALUES
                        ('2026-09-06', 81.1, 96.0),
                        ('2026-09-08', 80.4, 95.2)
                    """);
        }

        // Latest: the metric model is created and the legacy rows are carried over.
        flyway(null).migrate();
    }

    @Test
    void carriesEveryLegacyDayOverAsOneMeasurementPerMetric() throws SQLException {
        assertThat(count("SELECT count(*) FROM measurements")).isEqualTo(4);
        assertThat(count("SELECT count(*) FROM measurements WHERE metric = 'weight'")).isEqualTo(2);
        assertThat(count("SELECT count(*) FROM measurements WHERE metric = 'waist'")).isEqualTo(2);
    }

    @Test
    void keepsTheValuesOfEveryLegacyDayInTheReferenceUnit() throws SQLException {
        Map<String, BigDecimal> values = readValues("2026-09-06");

        assertThat(values).containsEntry("weight", new BigDecimal("81.1000"));
        assertThat(values).containsEntry("waist", new BigDecimal("96.0000"));
    }

    @Test
    void storesEveryCarriedOverMeasurementWithItsReferenceUnit() throws SQLException {
        assertThat(unitOf("2026-09-06", "weight")).isEqualTo("kg");
        assertThat(unitOf("2026-09-06", "waist")).isEqualTo("cm");
    }

    @Test
    void hasNoMeasurementWithoutAValueRow() throws SQLException {
        assertThat(count("""
                SELECT count(*) FROM measurements m
                WHERE NOT EXISTS (SELECT 1 FROM measurement_values v WHERE v.measurement_id = m.id)
                """)).isZero();
    }

    @Test
    void retiresTheLegacyTable() throws SQLException {
        assertThat(count(
                "SELECT count(*) FROM information_schema.tables WHERE table_name = 'measurements_legacy'"))
                .isZero();
    }

    @Test
    void seedsTheInitialMetricsAsAdmitted() throws SQLException {
        assertThat(count("SELECT count(*) FROM admitted_metrics")).isEqualTo(4);
    }

    private static Map<String, BigDecimal> readValues(String date) throws SQLException {
        Map<String, BigDecimal> values = new HashMap<>();
        try (Connection connection = open();
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery("""
                        SELECT m.metric, v.value
                        FROM measurements m
                        JOIN measurement_values v ON v.measurement_id = m.id
                        WHERE m.date = '%s'
                        """.formatted(date))) {
            while (rows.next()) {
                values.put(rows.getString("metric"), rows.getBigDecimal("value"));
            }
        }
        return values;
    }

    private static String unitOf(String date, String metric) throws SQLException {
        try (Connection connection = open();
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(
                        "SELECT unit FROM measurements WHERE date = '%s' AND metric = '%s'"
                                .formatted(date, metric))) {
            return rows.next() ? rows.getString("unit") : null;
        }
    }

    private static long count(String sql) throws SQLException {
        try (Connection connection = open();
                Statement statement = connection.createStatement();
                ResultSet rows = statement.executeQuery(sql)) {
            return rows.next() ? rows.getLong(1) : 0;
        }
    }

    private static Connection open() throws SQLException {
        return DriverManager.getConnection(
                jdbcUrl,
                PostgresIntegrationTest.POSTGRES.getUsername(),
                PostgresIntegrationTest.POSTGRES.getPassword());
    }

    private static Flyway flyway(String target) {
        var configuration = Flyway.configure()
                .dataSource(
                        jdbcUrl,
                        PostgresIntegrationTest.POSTGRES.getUsername(),
                        PostgresIntegrationTest.POSTGRES.getPassword());
        if (target != null) {
            configuration = configuration.target(target);
        }
        return configuration.load();
    }
}
