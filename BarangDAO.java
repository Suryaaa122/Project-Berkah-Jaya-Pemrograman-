package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import berkahjaya.model.Barang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class BarangDAO {

    private Connection conn;

    public BarangDAO() {
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

    public List<Barang> getAll() {
        checkConnection();
        List<Barang> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_barang ORDER BY nama_barang";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[BarangDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    public Barang getById(String idBarang) {
        checkConnection();
        String sql = "SELECT * FROM tb_barang WHERE id_barang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[BarangDAO] getById error: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Barang b) {
        checkConnection();
        String sql = "INSERT INTO tb_barang (id_barang, id_kategori, nama_barang, satuan, harga_jual, stok) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, b.getIdBarang());
            ps.setInt(2, b.getIdKategori());
            ps.setString(3, b.getNamaBarang());
            ps.setString(4, b.getSatuan());
            ps.setDouble(5, b.getHargaJual());
            ps.setInt(6, b.getStok());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Barang b) {
        checkConnection();
        String sql = "UPDATE tb_barang SET id_kategori=?, nama_barang=?, satuan=?, harga_jual=?, stok=? WHERE id_barang=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, b.getIdKategori());
            ps.setString(2, b.getNamaBarang());
            ps.setString(3, b.getSatuan());
            ps.setDouble(4, b.getHargaJual());
            ps.setInt(5, b.getStok());
            ps.setString(6, b.getIdBarang());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String idBarang) {
        checkConnection();
        String sql = "DELETE FROM tb_barang WHERE id_barang = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idBarang);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Proteksi jika barang sudah ada di riwayat transaksi penjualan
            if (e.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(null,
                    "Gagal menghapus! Produk ini tidak bisa dihapus karena sudah memiliki riwayat transaksi penjualan.",
                    "Proteksi Data Barang",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                System.err.println("[BarangDAO] delete error: " + e.getMessage());
            }
            return false;
        }
    }

    public boolean kurangiStok(Connection sharedConn, String idBarang, int jumlah) {
        String sql = "UPDATE tb_barang SET stok = stok - ? WHERE id_barang = ? AND stok >= ?";
        try (PreparedStatement ps = sharedConn.prepareStatement(sql)) {
            ps.setInt(1, jumlah);
            ps.setString(2, idBarang);
            ps.setInt(3, jumlah); 
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[BarangDAO] kurangiStok error: " + e.getMessage());
            return false;
        }
    }

    // --- METHOD BARU: GENERATE ID BARANG OTOMATIS ---
    public String generateIdBarang() {
        checkConnection();
        String newId = "BRG001";
        String sql = "SELECT MAX(RIGHT(id_barang, 3)) AS last_id FROM tb_barang";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("last_id");
                if (last != null) {
                    int nextNum = Integer.parseInt(last) + 1;
                    newId = String.format("BRG%03d", nextNum);
                }
            }
        } catch (SQLException e) {
            System.err.println("[BarangDAO] generateId error: " + e.getMessage());
        }
        return newId;
    }

    private Barang mapRow(ResultSet rs) throws SQLException {
        return new Barang(
            rs.getString("id_barang"),
            rs.getInt("id_kategori"),
            rs.getString("nama_barang"),
            rs.getString("satuan"),
            rs.getDouble("harga_jual"),
            rs.getInt("stok")
        );
    }
}