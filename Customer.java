package berkahjaya.model;

/**
 * Model Data Customer
 */
public class Customer {
    private String idCustomer;
    private String namaCustomer;
    private String alamat;
    private String telepon;

    // Konstruktor default
    public Customer() {}

    // Konstruktor lengkap
    public Customer(String idCustomer, String namaCustomer, String alamat, String telepon) {
        this.idCustomer   = idCustomer;
        this.namaCustomer = namaCustomer;
        this.alamat       = alamat;
        this.telepon      = telepon;
    }

    // Getter
    public String getIdCustomer()    { return idCustomer; }
    public String getNamaCustomer()  { return namaCustomer; }
    public String getAlamat()        { return alamat; }
    public String getTelepon()       { return telepon; }

    // Setter
    public void setIdCustomer(String idCustomer)      { this.idCustomer = idCustomer; }
    public void setNamaCustomer(String namaCustomer)  { this.namaCustomer = namaCustomer; }
    public void setAlamat(String alamat)              { this.alamat = alamat; }
    public void setTelepon(String telepon)            { this.telepon = telepon; }

    /**
     * Digunakan oleh JComboBox untuk menampilkan representasi teks objek
     */
    @Override
    public String toString() {
        return idCustomer + " - " + namaCustomer;
    }
}