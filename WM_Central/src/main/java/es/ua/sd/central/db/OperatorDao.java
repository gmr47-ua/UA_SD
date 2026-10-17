package es.ua.sd.central.db;
import java.sql.*;
import java.util.Optional;
import es.ua.sd.central.model.Operator;

public class OperatorDao {
    private final Database db;
    public OperatorDao(Database db) { this.db = db; }

    public void insert(String nombre, String email, String passwordHash) throws SQLException {
        String sql = "INSERT INTO Operators(Nombre, Email, Password) VALUES(?, ?, ?)";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.executeUpdate();
        }
    }

    public Optional<Operator> findByEmail(String email) throws SQLException {
        String sql = "SELECT ID, Nombre, Email, Password FROM Operators WHERE Email = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new Operator(
                    rs.getInt("ID"), rs.getString("Nombre"),
                    rs.getString("Email"), rs.getString("Password")));
            }
        }
    }

    public Optional<Operator> findByID(int ID) throws SQLException{
        String sql = "SELECT ID, Nombre, Email, Password FROM Operators WHERE ID = ?";
        try(Connection c = db.getConnection()){
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, ID);
            try(ResultSet rs = ps.executeQuery()){
                if(!rs.next()) return Optional.empty();
                return Optional.of(new Operator(
                    rs.getInt("ID"), rs.getString("Nombre"),
                    rs.getString("Email"),rs.getString("Password")));
            }
        }
    }
}
