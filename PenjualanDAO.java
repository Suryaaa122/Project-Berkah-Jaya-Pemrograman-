package berkahjaya.dao;

import berkahjaya.db.DBConnection;
import berkahjaya.model.DetailPenjualan;
import berkahjaya.model.Penjualan;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;

public class PenjualanDAO {

    private Connection conn;
    private final BarangDAO barangDAO;

    public PenjualanDAO() {
        this.conn = DBConnection.getConnection();
        this.barangDAO = new BarangDAO();
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

    public List<Penjualan> getRiwayatFiltered(String tglMulai, String tglSampai) {
        checkConnection();
        List<Penjualan> list = new ArrayList<>();
        
        // Join ke tb_barang DIHAPUS karena tabel utama cuma nyimpen totalan
        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.id_jual, p.tgl_transaksi, p.id_customer, p.total_bayar, p.id_user,
                c.nama_customer, 
                u.nama_lengkap AS nama_kasir_db
            FROM tb_penjualan p
            JOIN tb_customer c ON p.id_customer = c.id_customer
            JOIN tb_user     u ON p.id_user     = u.id_user
            WHERE 1=1
        """);

        if (tglMulai != null && !tglMulai.trim().isEmpty()) {
            sql.append(" AND p.tgl_transaksi >= '").append(tglMulai).append(" 00:00:00'");
        }
        if (tglSampai != null && !tglSampai.trim().isEmpty()) {
            sql.append(" AND p.tgl_transaksi <= '").append(tglSampai).append(" 23:59:59'");
        }

        sql.append(" ORDER BY p.tgl_transaksi DESC, p.id_jual DESC");

        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql.toString())) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("[PenjualanDAO] getRiwayatFiltered error: " + e.getMessage());
        }
        return list;
    }

    // --- METHOD BARU: TERIMA 2 PARAMETER ---
    public int simpanTransaksi(Penjualan p, List<DetailPenjualan> keranjang) {
        checkConnection();
        String sqlUtama = "INSERT INTO tb_penjualan (tgl_transaksi, id_customer, total_bayar, id_user) VALUES (?,?,?,?)";
        String sqlDetail = "INSERT INTO tb_detail_penjualan (id_jual, id_barang, jumlah_beli, sub_total) VALUES (?,?,?,?)";
        
        try {
            conn.setAutoCommit(false); 

            int idJualBaru = -1;

            // 1. Simpan Transaksi Utama (Ambil ID Jual yg auto-increment)
            try (PreparedStatement psUtama = conn.prepareStatement(sqlUtama, Statement.RETURN_GENERATED_KEYS)) {
                // Format lengkap dengan jam, menit, detik
SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
String tglFormat = sdf.format(p.getTglTransaksi());
psUtama.setString(1, tglFormat);
                psUtama.setString(2, p.getIdCustomer());
                psUtama.setDouble(3, p.getTotalBayar());
                psUtama.setInt(4, p.getIdUser());
                psUtama.executeUpdate();

                // Tarik ID Nota yang baru dibuat
                try (ResultSet rs = psUtama.getGeneratedKeys()) {
                    if (rs.next()) {
                        idJualBaru = rs.getInt(1);
                    }
                }
            }

            if (idJualBaru == -1) {
                conn.rollback();
                return 0; // Gagal bikin nota utama
            }

            // 2. Looping isi Keranjang, kurangi stok, lalu simpan ke Detail
            try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                for (DetailPenjualan detail : keranjang) {
                    
                    // Panggil BarangDAO lu buat ngurangin stok
                    boolean stokOk = barangDAO.kurangiStok(conn, detail.getIdBarang(), detail.getJumlahBeli());
                    
                    if (!stokOk) {
                        conn.rollback(); // Kalau ada 1 barang aja yg stoknya kurang, batalin semua transaksinya
                        return 0; 
                    }

                    // Antrikan untuk INSERT ke tb_detail_penjualan
                    psDetail.setInt(1, idJualBaru);
                    psDetail.setString(2, detail.getIdBarang());
                    psDetail.setInt(3, detail.getJumlahBeli());
                    psDetail.setDouble(4, detail.getSubTotal());
                    psDetail.addBatch(); // Kumpulin ke dalam batch
                }
                
                // Eksekusi insert banyak data sekaligus
                psDetail.executeBatch();
            }

            conn.commit(); 
            return idJualBaru;

        } catch (SQLException e) {
            System.err.println("[PenjualanDAO] simpanTransaksi error: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return -1;
        } finally {
            try {
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<Penjualan> getRiwayat() {
        return getRiwayatFiltered(null, null);
    }

    /**
 * Ambil riwayat penjualan lengkap dengan JOIN customer dan user
 * Return: List<Object[]> => [tglTransaksi, namaCustomer, totalBayar, namaUser]
 */
public List<Object[]> getRiwayatLengkap(String tglMulai, String tglSampai) {
    List<Object[]> list = new ArrayList<>();
    String sql;
    
    if (tglMulai == null || tglMulai.isEmpty() || tglSampai == null || tglSampai.isEmpty()) {
        // TANPA FILTER TANGGAL
        sql = "SELECT p.tgl_transaksi, c.nama_customer, p.total_bayar, u.nama_lengkap AS nama_user " +
              "FROM tb_penjualan p " +
              "JOIN tb_customer c ON p.id_customer = c.id_customer " +
              "JOIN tb_user u ON p.id_user = u.id_user " +
              "ORDER BY p.tgl_transaksi DESC, p.id_jual DESC";
    } else {
        // DENGAN FILTER TANGGAL
        sql = "SELECT p.tgl_transaksi, c.nama_customer, p.total_bayar, u.nama_lengkap AS nama_user " +
              "FROM tb_penjualan p " +
              "JOIN tb_customer c ON p.id_customer = c.id_customer " +
              "JOIN tb_user u ON p.id_user = u.id_user " +
              "WHERE p.tgl_transaksi >= ? AND p.tgl_transaksi < ? " +
              "ORDER BY p.tgl_transaksi DESC, p.id_jual DESC";
    }
    
    try (Connection conn = DBConnection.getConnection()) {
        PreparedStatement ps = conn.prepareStatement(sql);
        
        if (tglMulai != null && !tglMulai.isEmpty() && tglSampai != null && !tglSampai.isEmpty()) {
            // Format: yyyy-MM-dd HH:mm:ss
            ps.setString(1, tglMulai + " 00:00:00");
            // Untuk tglSampai, tambah 1 hari agar include full day
            ps.setString(2, tglSampai + " 23:59:59");
        }
        
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Object[] row = new Object[4];
            row[0] = rs.getTimestamp("tgl_transaksi"); // Ini akan ambil FULL timestamp (jam+menit+detik)
            row[1] = rs.getString("nama_customer");
            row[2] = rs.getDouble("total_bayar");
            row[3] = rs.getString("nama_user");
            list.add(row);
        }
    } catch (SQLException e) {
        System.err.println("[PenjualanDAO] getRiwayatLengkap error: " + e.getMessage());
        e.printStackTrace();
    }
    return list;
}

    private Penjualan mapRow(ResultSet rs) throws SQLException {
        Penjualan pj = new Penjualan();
        pj.setIdJual(rs.getInt("id_jual"));
        pj.setTglTransaksi(rs.getTimestamp("tgl_transaksi")); 
        pj.setIdCustomer(rs.getString("id_customer"));
        pj.setTotalBayar(rs.getDouble("total_bayar"));
        pj.setIdUser(rs.getInt("id_user"));
        
        
         pj.setNamaCustomer(rs.getString("nama_customer"));
         pj.setNamaUser(rs.getString("nama_kasir_db")); 
        
        return pj;
    }
    // --- METHOD BARU: AMBIL RINCIAN TRANSAKSI ---
    public List<DetailPenjualan> getDetailByNota(int idJual) {
        checkConnection();
        List<DetailPenjualan> list = new ArrayList<>();
        // Query pake JOIN biar nama barangnya ikut kebawa dari tb_barang
        String sql = "SELECT d.*, b.nama_barang FROM tb_detail_penjualan d JOIN tb_barang b ON d.id_barang = b.id_barang WHERE d.id_jual = ?";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idJual);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailPenjualan dp = new DetailPenjualan();
                    dp.setIdDetail(rs.getInt("id_detail"));
                    dp.setIdJual(rs.getInt("id_jual"));
                    dp.setIdBarang(rs.getString("id_barang"));
                    dp.setJumlahBeli(rs.getInt("jumlah_beli"));
                    dp.setSubTotal(rs.getDouble("sub_total"));
                    dp.setNamaBarang(rs.getString("nama_barang")); // Tarik dari alias join
                    list.add(dp);
                }
            }
        } catch (SQLException e) {
            System.err.println("[PenjualanDAO] getDetailByNota error: " + e.getMessage());
        }
        return list;
    }
}