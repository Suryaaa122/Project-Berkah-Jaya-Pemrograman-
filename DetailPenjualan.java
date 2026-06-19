package berkahjaya.model;

public class DetailPenjualan {
    private int idDetail;
    private int idJual;
    private String idBarang;
    private int jumlahBeli;
    private double subTotal;

    // --- Tambahan untuk nampilin nama di UI ---
    private String namaBarang; 

    public DetailPenjualan() {}

    public DetailPenjualan(int idJual, String idBarang, int jumlahBeli, double subTotal) {
        this.idJual = idJual;
        this.idBarang = idBarang;
        this.jumlahBeli = jumlahBeli;
        this.subTotal = subTotal;
    }

    // Getter & Setter
    public int getIdDetail() { return idDetail; }
    public void setIdDetail(int idDetail) { this.idDetail = idDetail; }
    public int getIdJual() { return idJual; }
    public void setIdJual(int idJual) { this.idJual = idJual; }
    public String getIdBarang() { return idBarang; }
    public void setIdBarang(String idBarang) { this.idBarang = idBarang; }
    public int getJumlahBeli() { return jumlahBeli; }
    public void setJumlahBeli(int jumlahBeli) { this.jumlahBeli = jumlahBeli; }
    public double getSubTotal() { return subTotal; }
    public void setSubTotal(double subTotal) { this.subTotal = subTotal; }
    
    // --- Getter Setter Baru ---
    public String getNamaBarang() { return namaBarang; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
}