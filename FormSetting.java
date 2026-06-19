package berkahjaya.ui;

import berkahjaya.ui.Settings;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class FormSetting extends JPanel {

    public FormSetting() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(41, 128, 185), 1, true),
            "  PENGATURAN SISTEM GLOBAL  ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(41, 128, 185)));
        
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(15, 15, 15, 15);
        g.anchor = GridBagConstraints.WEST;
        
        JCheckBox chk = new JCheckBox("Aktifkan Pajak PPN (11%) untuk Semua Transaksi Toko");
        chk.setFont(new Font("Segoe UI", Font.BOLD, 14));
        chk.setSelected(Settings.isPakaiPpn()); 
        
        JButton btnSimpan = new JButton("💾 Simpan Pengaturan");
        btnSimpan.setFont(new Font("Dialog", Font.BOLD, 12));
        btnSimpan.setBackground(new Color(26, 95, 122));
        btnSimpan.setForeground(Color.WHITE);
        btnSimpan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSimpan.addActionListener(e -> {
            Settings.setPakaiPpn(chk.isSelected()); // Simpan permanen ke file config
            JOptionPane.showMessageDialog(this, "Pengaturan PPN Toko Berhasil Diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            
        });
        
        g.gridx = 0; g.gridy = 0; p.add(chk, g);
        g.gridy = 1; p.add(btnSimpan, g);
        add(p);
    }
}