package es.ua.sd.central.db;
import java.sql.*;
import java.io.IOException;
import java.nio.file.*;

public class Database {
    private final String url;

    public Database(String dbPath) throws IOException {
        Files.createDirectories(Path.of(dbPath).toAbsolutePath().getParent());
        this.url = "jdbc:sqlite:" + dbPath;
        initSchema();
    }

    public Connection getConnection() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA busy_timeout=5000");
            st.execute("PRAGMA foreign_keys=ON");
        }
        return c;
    }

    private void initSchema() {
        try (Connection c = getConnection(); Statement st = c.createStatement()) {
            st.execute("PRAGMA journal_mode=WAL");
            st.execute("""
                CREATE TABLE IF NOT EXISTS Operators (
                        ID       INTEGER PRIMARY KEY,
                        Nombre   TEXT NOT NULL,
                        Email    TEXT NOT NULL UNIQUE,
                        Password TEXT NOT NULL
                    )
                """);

            st.execute(
                """
                CREATE TABLE IF NOT EXISTS WaterStations (
                        ID     TEXT PRIMARY KEY,
                        LOCATION TEXT NOT NULL,
                        Status TEXT NOT NULL DEFAULT 'DESCONECTADA'
                """
            );

            st.execute(
                """
                CREATE TABLE IF NOT EXISTS Irrigations (
                ID          INTEGER PRIMARY KEY,
                StationID   TEXT NOT NULL REFERENCES WaterStations(ID),
                OperatorID  INTEGER REFERENCES Operators(ID),
                StartedAt   TEXT NOT NULL,
                EndedAt     TEXT,
                Volume      REAL,
                Result      TEXT
            )
            CREATE UNIQUE INDEX IF NOT EXISTS idx_one_open_irrigation
            ON Irrigations(StationID) WHERE EndedAt IS NULL
            """
            );
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la BD", e);
        }
    }
}
