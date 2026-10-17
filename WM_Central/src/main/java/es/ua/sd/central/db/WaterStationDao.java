package es.ua.sd.central.db;

import es.ua.sd.central.model.StationStatus;
import es.ua.sd.central.model.WaterStation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class WaterStationDao {

    private final Database database;

    public WaterStationDao(Database database) {
        this.database = database;
    }

    public void insert(WaterStation station) throws SQLException {
        String sql = "INSERT INTO WaterStations (ID, Location, Status) VALUES (?, ?, ?)";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, station.id());
            ps.setString(2, station.location());
            ps.setString(3, station.status().name());
            ps.executeUpdate();
        }
    }

    public Optional<WaterStation> findById(String id) throws SQLException {
        String sql = "SELECT ID, Location, Status FROM WaterStations WHERE ID = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<WaterStation> findAll() throws SQLException {
        String sql = "SELECT ID, Location, Status FROM WaterStations ORDER BY ID";
        List<WaterStation> result = new ArrayList<>();
        try (Connection conn = database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(map(rs));
            }
        }
        return result;
    }

    /** @return true si existía la estación y se actualizó. */
    public boolean updateStatus(String id, StationStatus status) throws SQLException {
        String sql = "UPDATE WaterStations SET Status = ? WHERE ID = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setString(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Lo usa StationService.resetAllToDisconnected() al arrancar CENTRAL. */
    public void updateAllStatus(StationStatus status) throws SQLException {
        String sql = "UPDATE WaterStations SET Status = ?";
        try (Connection conn = database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.executeUpdate();
        }
    }

    private WaterStation map(ResultSet rs) throws SQLException {
        return new WaterStation(
                rs.getString("ID"),
                rs.getString("Location"),
                StationStatus.valueOf(rs.getString("Status")));
    }
}
