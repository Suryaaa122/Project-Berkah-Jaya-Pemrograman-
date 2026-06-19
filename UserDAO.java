package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import berkahjaya.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // 1. Ambil semua data user (Pake tb_user)
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_user ORDER BY id_user ASC";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                User u = new User(
                    rs.getInt("id_user"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("nama_lengkap"), // Menggunakan nama_lengkap
                    rs.getString("level")         // Menggunakan level
                );
                list.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Gagal Load Data User: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    // 2. Tambah User Baru
    public boolean insert(User user) {
        String sql = "INSERT INTO tb_user (username, password, nama_lengkap, level) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword()); 
            ps.setString(3, user.getNamaLengkap());
            ps.setString(4, user.getLevel());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal Insert User: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 3. Ubah Data User
    public boolean update(User user) {
        String sql = "UPDATE tb_user SET username=?, password=?, nama_lengkap=?, level=? WHERE id_user=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getNamaLengkap());
            ps.setString(4, user.getLevel());
            ps.setInt(5, user.getIdUser());
            
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal Update User: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 4. Hapus User
    public boolean delete(int idUser) {
        String sql = "DELETE FROM tb_user WHERE id_user=?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, idUser);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Gagal Delete User: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}