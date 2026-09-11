package com.careme.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import com.careme.backend.repository.MeasurementJpaDao;

/**
 * Base for tests that need a real PostgreSQL database.
 *
 * <p>The container is started once per JVM and reused by every subclass, so the
 * schema is always built by the real Flyway migration on the real engine instead
 * of an in-memory approximation. Each test starts from an empty table.
 */
public abstract class PostgresIntegrationTest {

    @ServiceConnection
    public static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    static {
        POSTGRES.start();
    }

    @Autowired
    protected MeasurementJpaDao measurementJpaDao;

    @BeforeEach
    protected void clearMeasurements() {
        // Bulk delete: it issues the DELETE immediately. A plain `deleteAll()`
        // would only queue row deletions, and Hibernate flushes inserts before
        // deletions, so a test that inserts would hit the rows left behind.
        measurementJpaDao.deleteAllInBatch();
    }
}
