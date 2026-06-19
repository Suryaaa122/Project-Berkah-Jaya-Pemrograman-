package berkahjaya.ui;

import berkahjaya.dao.LoginDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

public class FormLogin extends JDialog {
    private JTextField tfUser;
    private JPasswordField tfPass;
    private int idUserSession = -1; 
    private String namaUserAktif = ""; 
    private String levelUserSession = ""; 
    
    private final LoginDAO loginDAO = new LoginDAO();

    public FormLogin() {
        setTitle("Login Admin");
        setModal(true); 
        setResizable(false);
        setSize(900, 450); // Ukuran diperbesar untuk menampung gambar
        initComponents();
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Membagi layar jadi Kiri dan Tengah(Kanan)

        // ==========================================
        // 1. PANEL KIRI: GAMBAR MASKOT (bek.jpeg)
        // ==========================================
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(new Color(25, 30, 35)); 
        leftPanel.setPreferredSize(new Dimension(400, 450));
        
        try {
            URL imgURL = getClass().getResource("/resources/bek.jpeg"); 
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                
                // === LOGIKA OBJECT-FIT COVER (PENUH & PROPORSIONAL) ===
                // Samakan ukurannya dengan leftPanel.setPreferredSize(350, 450)
                int panelWidth = 350; 
                int panelHeight = 450;
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();
                
                // Gunakan Math.max! 
                // Gambar akan diperbesar menutupi seluruh tinggi panel, 
                // dan sisa lebarnya akan otomatis terpotong (tersembunyi) dengan rapi.
                double ratio = Math.max((double) panelWidth / imgWidth, (double) panelHeight / imgHeight);
                int newWidth = (int) (imgWidth * ratio);
                int newHeight = (int) (imgHeight * ratio);
                
                Image img = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                JLabel lblImage = new JLabel(new ImageIcon(img));
                
                lblImage.setHorizontalAlignment(SwingConstants.CENTER);
                lblImage.setVerticalAlignment(SwingConstants.CENTER);
                
                leftPanel.add(lblImage, BorderLayout.CENTER);
            } else {
                JLabel lblAlt = new JLabel("GAMBAR MASKOT", SwingConstants.CENTER);
                lblAlt.setForeground(Color.WHITE);
                leftPanel.add(lblAlt, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        add(leftPanel, BorderLayout.WEST);

        // ==========================================
        // 2. PANEL KANAN: FORM INPUT LOGIN
        // ==========================================
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new EmptyBorder(25, 100, 25, 100));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblEmoji = new JLabel("LOGIN BANG", JLabel.CENTER); 
        lblEmoji.setFont(new Font("Segoe UI Emoji", Font.BOLD, 24));
        lblEmoji.setForeground(new Color(52, 152, 219)); 
        
        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        g.insets = new Insets(0, 0, 30, 0);
        p.add(lblEmoji, g);

        // --- BARIS USERNAME ---
        g.gridwidth = 1; g.gridy = 1; g.gridx = 0;
        g.insets = new Insets(8, 8, 8, 8);
        g.weightx = 0.0; 
        JLabel lblUser = new JLabel("Username:");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblUser, g);
        
        tfUser = new JTextField(); 
        tfUser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfUser.setPreferredSize(new Dimension(0, 35));
        g.gridx = 1; 
        g.weightx = 1.0;
        p.add(tfUser, g);

        // --- BARIS PASSWORD ---
        g.gridx = 0; g.gridy = 2;
        g.weightx = 0.0; 
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 14));
        p.add(lblPass, g);
        
        tfPass = new JPasswordField(); 
        tfPass.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tfPass.setPreferredSize(new Dimension(0, 35)); 
        g.gridx = 1; 
        g.weightx = 1.0; 
        p.add(tfPass, g);

        // --- TOMBOL LOGIN ---
        JButton btnLogin = new JButton("MASUK KE SISTEM");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(52, 152, 219));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 40)); 
        
        KeyAdapter enterAction = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) prosesLogin();
            }
        };
        tfUser.addKeyListener(enterAction);
        tfPass.addKeyListener(enterAction);
        btnLogin.addActionListener(e -> prosesLogin());
        
        g.gridx = 0; g.gridy = 3; g.gridwidth = 2;
        g.weightx = 1.0; // Tombol 
        g.insets = new Insets(35, 8, 5, 8);
        p.add(btnLogin, g);

        add(p, BorderLayout.CENTER);
    }
        
    private void prosesLogin() {
        String u = tfUser.getText().trim();
        String p = new String(tfPass.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username/Password tidak boleh kosong!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int result = loginDAO.login(u, p);
        if (result != -1) {
            this.idUserSession = result;
            this.namaUserAktif = loginDAO.getNamaLengkapById(result); 
            this.levelUserSession = loginDAO.getLevelById(result);
            
            sec.SysCache.initProcess(this.idUserSession);
            this.dispose(); 
        } else {
            JOptionPane.showMessageDialog(this, "Username atau Password salah!");
        }
    }

    public int getIdUserSession() { return idUserSession; }
    public String getNamaUserAktif() { return (namaUserAktif == null || namaUserAktif.isEmpty()) ? "Unknown User" : namaUserAktif; }
    public String getLevelUserSession() { return (levelUserSession == null || levelUserSession.isEmpty()) ? "Petugas" : levelUserSession; }
}