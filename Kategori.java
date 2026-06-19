package berkahjaya.model;

public class Kategori {
    private int idKategori;
    private String namaKategori;

    // Constructor Kosong
    public Kategori() {}

    // Constructor Lengkap
    public Kategori(int idKategori, String namaKategori) {
        this.idKategori = idKategori;
        this.namaKategori = namaKategori;
    }

    // Getter dan Setter
    public int getIdKategori() { return idKategori; }
    public void setIdKategori(int idKategori) { this.idKategori = idKategori; }

    public String getNamaKategori() { return namaKategori; }
    public void setNamaKategori(String namaKategori) { this.namaKategori = namaKategori; }

    // Method toString ini penting banget biar JComboBox otomatis nampilin teks yang rapi
    @Override
    public String toString() {
        return idKategori + " - " + namaKategori;
    }
}