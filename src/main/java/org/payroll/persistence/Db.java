package org.payroll.persistence;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class Db {
    private Db() {}

    /**
     * Open (or create) a SQLite database at the given path and apply safe defaults.
     * Caller is responsible for closing the returned Connection.
     */
    public static Connection open(Path path) throws Exception {
        Files.createDirectories(path.toAbsolutePath().getParent());

        // Establish connection
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + path.toAbsolutePath());

        // Pragmas: correctness first, then reasonable performance for a single-user laptop
        try (Statement s = c.createStatement()) {
            // Enforce referential integrity
            s.execute("PRAGMA foreign_keys = ON");

            // Write-ahead logging: better concurrency + durability for desktop apps
            s.execute("PRAGMA journal_mode = WAL");

            // Reasonable sync level for WAL on a laptop
            s.execute("PRAGMA synchronous = NORMAL");

            // Avoid "database is locked" hiccups during brief contention
            s.execute("PRAGMA busy_timeout = 5000");

            // Slightly faster temp objects; safe on a desktop
            s.execute("PRAGMA temp_store = MEMORY");
        }

        return c;
    }
}