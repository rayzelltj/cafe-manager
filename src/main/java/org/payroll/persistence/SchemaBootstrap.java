package org.payroll.persistence;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

/**
 * Ensures the database schema exists. Uses PRAGMA user_version for simple, forward-only versioning.
 * - If user_version == 0  => run full schema file and set to SCHEMA_VERSION.
 * - If user_version < SCHEMA_VERSION => throw for now (add migrations later).
 * - If user_version >= SCHEMA_VERSION => do nothing.
 */
public final class SchemaBootstrap {
    private SchemaBootstrap() {}

    // Bump this when you add migrations
    private static final int SCHEMA_VERSION = 1;

    public static void ensure(Connection c) throws SQLException {
        int current = getUserVersion(c);

        if (current == 0) {
            // Fresh DB: apply full schema atomically
            String ddl = readResource("/db/schema_payroll.sql");
            try (Statement s = c.createStatement()) {
                c.setAutoCommit(false);
                try {
                    s.executeUpdate(ddl); // SQLite JDBC supports multi-statement executes
                    setUserVersion(c, SCHEMA_VERSION);
                    c.commit();
                } catch (SQLException e) {
                    c.rollback();
                    throw e;
                } finally {
                    c.setAutoCommit(true);
                }
            }
        } else if (current < SCHEMA_VERSION) {
            // Place to implement incremental migrations later (V1 -> V2, etc.)
            // For now, fail loudly to avoid running with a partial schema.
            throw new SQLException("Database schema is outdated (have " + current +
                    ", need " + SCHEMA_VERSION + "). Add migrations.");
        }
        // else: already at or above SCHEMA_VERSION; nothing to do.
    }

    private static int getUserVersion(Connection c) throws SQLException {
        try (Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("PRAGMA user_version")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private static void setUserVersion(Connection c, int version) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA user_version = " + version);
        }
    }

    private static String readResource(String path) {
        try (InputStream in = SchemaBootstrap.class.getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("Missing resource: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read resource " + path, e);
        }
    }
}