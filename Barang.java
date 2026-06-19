package berkahjaya.model;

/**
 * Model Data Barang
 */
public class Barang {
    private String idBarang;
    private int idKategori;
    private String namaBarang;
    private String satuan;
    private double hargaJual;
    private int stok;

    public Barang() {}

    public Barang(String idBarang, int idKategori, String namaBarang,
                  String satuan, double hargaJual, int stok) {
        this.idBarang   = idBarang;
        this.idKategori = idKategori;
        this.namaBarang = namaBarang;
        this.satuan     = satuan;
        this.hargaJual  = hargaJual;
        this.stok       = stok;
    }

    // Getter
    public String getIdBarang()    { return idBarang; }
    public int    getIdKategori()  { return idKategori; }
    public String getNamaBarang()  { return namaBarang; }
    public String getSatuan()      { return satuan; }
    public double getHargaJual()   { return hargaJual; }
    public int    getStok()        { return stok; }

    // Setter dengan Validasi Sederhana
    public void setIdBarang(String idBarang)     { this.idBarang = idBarang; }
    public void setIdKategori(int idKategori)    { this.idKategori = idKategori; }
    public void setNamaBarang(String namaBarang) { this.namaBarang = namaBarang; }
    public void setSatuan(String satuan)         { this.satuan = satuan; }
    
    public void setHargaJual(double hargaJual)   { 
        // Proteksi: Harga tidak boleh negatif
        this.hargaJual = Math.max(0, hargaJual); 
    }
    
    public void setStok(int stok) { 
        // Proteksi: Stok tidak boleh negatif
        this.stok = Math.max(0, stok); 
    }

    @Override
    public String toString() {
        return idBarang + " - " + namaBarang;
    }
}