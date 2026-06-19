package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import berkahjaya.model.Kategori;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class KategoriDAO {
    private Connection conn;

    public KategoriDAO() {
        this.conn = DBConnection.getConnection();
    }

    private void checkConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DBConnection.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ambil Semua Data Kategori
    public List<Kategori> getAll() {
        checkConnection();
        List<Kategori> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_kategori ORDER BY id_kategori";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Kategori(rs.getInt("id_kategori"), rs.getString("nama_kategori")));
            }
        } catch (SQLException e) {
            System.err.println("[KategoriDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    public boolean insert(Kategori k) {
        checkConnection();
        String sql = "INSERT INTO tb_kategori (id_kategori, nama_kategori) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, k.getIdKategori());
            ps.setString(2, k.getNamaKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KategoriDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    // Update Nama Kategori
    public boolean update(Kategori k) {
        checkConnection();
        String sql = "UPDATE tb_kategori SET nama_kategori=? WHERE id_kategori=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKategori());
            ps.setInt(2, k.getIdKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[KategoriDAO] update error: " + e.getMessage());
            return false;
        }
    }
    
    public int generateIdKategori() {
        checkConnection();
        String sql = "SELECT MAX(id_kategori) AS last_id FROM tb_kategori";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                int last = rs.getInt("last_id");
                return last + 1; 
            }
        } catch (SQLException e) {
            System.err.println("[KategoriDAO] generateId error: " + e.getMessage());
        }
        return 1; 
    }
    
    // Hapus Kategori dengan Proteksi Foreign Key
    public boolean delete(int idKategori) {
        checkConnection();
        String sql = "DELETE FROM tb_kategori WHERE id_kategori = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKategori);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(null,
                    "Gagal menghapus! Kategori ini tidak bisa dihapus karena masih digunakan oleh beberapa barang.",
                    "Proteksi Master Data", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                System.err.println("[KategoriDAO] delete error: " + e.getMessage());
            }
            return false;
        }
    }
}