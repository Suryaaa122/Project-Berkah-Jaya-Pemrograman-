package berkahjaya.ui;

import java.io.*;
import java.util.Properties;

public class Settings {
    private static final String FILE_NAME = "config.properties";
    private static Properties prop = new Properties();

    static {
        // Otomatis membaca file konfigurasi saat aplikasi pertama kali dibuka
        try (InputStream is = new FileInputStream(FILE_NAME)) {
            prop.load(is);
        } catch (IOException e) {
            // Jika file belum ada (aplikasi baru di-install), set default: PPN OFF
            prop.setProperty("pakai_ppn", "false");
        }
    }

    public static boolean isPakaiPpn() {
        return Boolean.parseBoolean(prop.getProperty("pakai_ppn", "false"));
    }

    public static void setPakaiPpn(boolean val) {
        prop.setProperty("pakai_ppn", String.valueOf(val));
        try (OutputStream os = new FileOutputStream(FILE_NAME)) {
            prop.store(os, "Berkah Jaya System Configuration");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}