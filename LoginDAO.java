package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import java.sql.*;

public class LoginDAO {
    private Connection conn;

    public LoginDAO() {
        this.conn = DBConnection.getConnection();
    }

    public int login(String username, String password) {
        String sql = "SELECT id_user FROM tb_user WHERE username = ? AND password = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_user");
                }
            }
        } catch (SQLException e) {
            System.err.println("LoginDAO Error: " + e.getMessage());
        }
        return -1;
    }

    public String getNamaLengkapById(int id) {
        String sql = "SELECT nama_lengkap FROM tb_user WHERE id_user = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nama_lengkap");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown User";
    }

    // [BARU] Method untuk mengambil data level berdasarkan ID
    public String getLevelById(int id) {
        String sql = "SELECT level FROM tb_user WHERE id_user = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("level");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Petugas"; 
    }
}