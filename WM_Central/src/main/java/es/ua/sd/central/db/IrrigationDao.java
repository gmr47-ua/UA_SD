package es.ua.sd.central.db;
import java.sql.*;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import es.ua.sd.central.model.*;

public class IrrigationDao {
    private final Database db;
    public IrrigationDao(Database db) { this.db = db; }

    /** Abre un riego y devuelve su ID. operatorId puede ser null (orden de Central). */
    public int start(Connection c, String stationId, Integer operatorId) throws SQLException {
        String sql = "INSERT INTO Irrigations(StationID, OperatorID, StartedAt) VALUES(?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, stationId);
            if (operatorId == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, operatorId);
            ps.setString(3, Instant.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Cierra el riego en curso de una estación. Devuelve false si no había ninguno abierto. */
    public boolean finish(Connection c, String stationId, double volume,
                          IrrigationResult result) throws SQLException {
        String sql = """
            UPDATE Irrigations SET EndedAt = ?, Volume = ?, Result = ?
            WHERE StationID = ? AND EndedAt IS NULL""";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Instant.now().toString());
            ps.setDouble(2, volume);
            ps.setString(3, result.name());
            ps.setString(4, stationId);
            return ps.executeUpdate() > 0;
        }
    }

    public Optional<Irrigation> findOpenByStation(String stationId) throws SQLException {
        String sql = "SELECT * FROM Irrigations WHERE StationID = ? AND EndedAt IS NULL";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Irrigation> findByStation(String stationId) throws SQLException {
        String sql = "SELECT * FROM Irrigations WHERE StationID = ? ORDER BY StartedAt DESC";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, stationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Irrigation> list = new ArrayList<>();
                while (rs.next()) list.add(map(rs));
                return list;
            }
        }
    }

    private Irrigation map(ResultSet rs) throws SQLException {
        int op = rs.getInt("OperatorID");
        Integer operatorId = rs.wasNull() ? null : op;
        double vol = rs.getDouble("Volume");
        Double volume = rs.wasNull() ? null : vol;
        String end = rs.getString("EndedAt");
        return new Irrigation(
            rs.getInt("ID"), rs.getString("StationID"), operatorId,
            Instant.parse(rs.getString("StartedAt")),
            end == null ? null : Instant.parse(end),
            volume, rs.getString("Result"));
    }

}
