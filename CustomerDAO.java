package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import berkahjaya.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

public class CustomerDAO {

    private Connection conn;

    public CustomerDAO() {
        this.conn = DBConnection.getConnection();
    }

    /**
     * Memastikan koneksi tidak null atau tertutup sebelum digunakan
     */
    private void checkConnection() {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DBConnection.getConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Customer> getAll() {
        checkConnection();
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM tb_customer ORDER BY nama_customer";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] getAll error: " + e.getMessage());
        }
        return list;
    }

    public Customer getById(String idCustomer) {
        checkConnection();
        String sql = "SELECT * FROM tb_customer WHERE id_customer = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCustomer);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] getById error: " + e.getMessage());
        }
        return null;
    }

    public boolean insert(Customer c) {
        checkConnection();
        // Menggunakan kolom eksplisit lebih aman jika struktur tabel berubah di masa depan
        String sql = "INSERT INTO tb_customer (id_customer, nama_customer, alamat, telepon) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getIdCustomer());
            ps.setString(2, c.getNamaCustomer());
            ps.setString(3, c.getAlamat());
            ps.setString(4, c.getTelepon());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean update(Customer c) {
        checkConnection();
        String sql = "UPDATE tb_customer SET nama_customer=?, alamat=?, telepon=? WHERE id_customer=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNamaCustomer());
            ps.setString(2, c.getAlamat());
            ps.setString(3, c.getTelepon());
            ps.setString(4, c.getIdCustomer());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] update error: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String idCustomer) {
        checkConnection();
        String sql = "DELETE FROM tb_customer WHERE id_customer = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, idCustomer);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 1451) {
                JOptionPane.showMessageDialog(null,
                    "Gagal menghapus! Customer ini tidak bisa dihapus karena memiliki riwayat transaksi di toko.",
                    "Proteksi Data",
                    JOptionPane.WARNING_MESSAGE);
            } else {
                System.err.println("[CustomerDAO] delete error: " + e.getMessage());
            }
            return false;
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getString("id_customer"),
            rs.getString("nama_customer"),
            rs.getString("alamat"),
            rs.getString("telepon")
        );
    }
    // --- METHOD TAMBAHAN UNTUK AUTO-ID ---
    public String generateIdCustomer() {
        checkConnection();
        String newId = "CST001"; // Default ID kalau tabel masih kosong
        String sql = "SELECT MAX(RIGHT(id_customer, 3)) AS last_id FROM tb_customer";
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                String last = rs.getString("last_id");
                if (last != null) {
                    int nextNum = Integer.parseInt(last) + 1;
                    newId = String.format("CST%03d", nextNum); // Format jadi CST002, dst
                }
            }
        } catch (SQLException e) {
            System.err.println("[CustomerDAO] generateId error: " + e.getMessage());
        }
        return newId;
    }
}