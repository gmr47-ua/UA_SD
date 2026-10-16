package es.ua.sd.central.db;
import java.sql.*;
import java.util.Optional;

public class WaterStationDao {
    private final Database db;
    public WaterStationDao(Database db) { this.db = db; }

    // Alta o actualización de estado (el Monitor se conecta y reporta)
    public void upsert(String id, String status) throws SQLException {
        String sql = """
            INSERT INTO WaterStations(ID, Status) VALUES(?, ?)
            ON CONFLICT(ID) DO UPDATE SET Status = excluded.Status""";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, status);
            ps.executeUpdate();
        }
    }

    public Optional<String> findStatus(String id) throws SQLException {
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT Status FROM WaterStations WHERE ID = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getString(1)) : Optional.empty();
            }
        }
    }
}
