package berkahjaya.model;

import java.util.Date;

/**
 * Model Data Penjualan (VERSI BARU)
 * Hanya menyimpan data nota utama. Detail barang pindah ke DetailPenjualan.java
 */
public class Penjualan {
    private int idJual;
    private Date tglTransaksi;
    private String idCustomer;
    private double totalBayar;
    private int idUser;

    // Field tambahan untuk keperluan reporting (JOIN)
    private String namaCustomer;
    private String namaUser;

    public Penjualan() {}

    // --- INI CONSTRUCTOR BARU YANG CUMA MINTA 4 DATA ---
    public Penjualan(Date tglTransaksi, String idCustomer, double totalBayar, int idUser) {
        this.tglTransaksi = tglTransaksi;
        this.idCustomer = idCustomer;
        this.totalBayar = totalBayar;
        this.idUser = idUser;
    }

    // Getter
    public int getIdJual() { return idJual; }
    public Date getTglTransaksi() { return tglTransaksi; }
    public String getIdCustomer() { return idCustomer; }
    public double getTotalBayar() { return totalBayar; }
    public int getIdUser() { return idUser; }
    public String getNamaCustomer() { return namaCustomer; }
    public String getNamaUser() { return namaUser; }

    // Setter
    public void setIdJual(int idJual) { this.idJual = idJual; }
    public void setTglTransaksi(Date tglTransaksi) { this.tglTransaksi = tglTransaksi; }
    public void setIdCustomer(String idCustomer) { this.idCustomer = idCustomer; }
    public void setTotalBayar(double totalBayar) { this.totalBayar = totalBayar; }
    public void setIdUser(int idUser) { this.idUser = idUser; }
    public void setNamaCustomer(String nc) { this.namaCustomer = nc; }
    public void setNamaUser(String nu) { this.namaUser = nu; } 
}