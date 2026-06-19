package berkahjaya.ui;

import berkahjaya.db.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;

public class MainFrame extends JFrame {

    private JTabbedPane tabbedPane;
    private FormTransaksi  panelTransaksi;
    private FormBarang     panelBarang;
    private FormCustomer   panelCustomer;
    private LaporanPanel   panelLaporan;
    private FormUser       panelUser;
    private FormLogSession  panelLogSession;
    private final int idUserLogin;
    private final String namaUserLogin; 
    private final String levelUserLogin; 

    // Constructor diubah agar menerima id, nama, dan level
    public MainFrame(int idUser, String namaUser, String levelUser) {
        this.idUserLogin = idUser;
        this.namaUserLogin = namaUser;
        this.levelUserLogin = levelUser; // Simpan level user dari database
        initComponents();
        
        setTitle("Berkah Jaya System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 800); 
        setMinimumSize(new Dimension(1100, 700));
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        // ---- 1. HEADER (Clean & Dynamic) ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(25, 30, 35)); 
        header.setPreferredSize(new Dimension(0, 85));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(50, 55, 60)));

        // Sisi Kiri: Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        brandPanel.setOpaque(false);

        try {
            // Pastikan pakai file PNG yang baru di-resize
            String path = "/resources/logo.png"; 
            URL imgURL = getClass().getResource(path);
            
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(55, 55, Image.SCALE_SMOOTH);
                brandPanel.add(new JLabel(new ImageIcon(img)));
            } else {
                JLabel lblEmoji = new JLabel("\uD83D\uDED2");
                lblEmoji.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
                brandPanel.add(lblEmoji);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel lblTitle = new JLabel("BERKAH JAYA");
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        brandPanel.add(lblTitle);
        header.add(brandPanel, BorderLayout.WEST);

        // Sisi Kanan: Profile & Logout (Dinamis)
        JPanel userNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 25, 22));
        userNav.setOpaque(false);

        // Menampilkan Level asli dari database
        JLabel lblProfile = new JLabel("<html><div style='text-align: right;'>"
        + "<span style='color: #FFFFFF; font-size: 13px; font-weight: bold;'>" + namaUserLogin.toUpperCase() + "</span><br>"
        + "<span style='color: #BDC3C7; font-size: 11px; font-weight: bold;'>" + levelUserLogin + " (ID: #" + idUserLogin + ")</span>"
        + "</div></html>");
        
        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setFocusPainted(false);
        btnLogout.setBackground(new Color(180, 60, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI Emoji", Font.BOLD, 12)); 
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.setPreferredSize(new Dimension(100, 35));
        btnLogout.addActionListener(e -> aksiLogout());

        userNav.add(lblProfile);
        userNav.add(Box.createRigidArea(new Dimension(10, 0))); 
        userNav.add(btnLogout);
        header.add(userNav, BorderLayout.EAST);

        // ---- 2. MAIN CONTENT (Sidebar) ----
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT); 
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.putClientProperty("JTabbedPane.tabInsets", new Insets(15, 25, 15, 25));

        panelTransaksi = new FormTransaksi(idUserLogin, namaUserLogin);
        panelBarang    = new FormBarang();
        panelCustomer  = new FormCustomer();
        panelLaporan   = new LaporanPanel();

        // Tab umum untuk semua level
        tabbedPane.addTab(navItem("⇄", "Transaksi"), panelTransaksi);
        tabbedPane.addTab(navItem("📦", "Data Barang"), panelBarang);
        tabbedPane.addTab(navItem("👤", "Customer"), panelCustomer);
        tabbedPane.addTab(navItem("📊", "Laporan"), panelLaporan);

        // LOGIKA HAK AKSES: Hanya ditambahkan jika levelnya "Admin"
        if ("Admin".equalsIgnoreCase(levelUserLogin)) {
            panelUser = new FormUser();
            tabbedPane.addTab(navItem("⚙️", "Manajemen User"), panelUser);
            panelLogSession = new FormLogSession();
            tabbedPane.addTab(navItem("🕒", "Log Sesi Kasir"), panelLogSession);
            FormSetting panelSetting = new FormSetting();
            tabbedPane.addTab(navItem("🛠️", "Pengaturan Toko"), panelSetting);
        }

        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) { panelTransaksi.refreshComboBoxes(); panelTransaksi.updateStatusPajakSistem(); }
            if (idx == 3) panelLaporan.loadData(null, null); 
            if (panelLogSession != null && tabbedPane.getSelectedComponent() == panelLogSession) {
                panelLogSession.loadLogData();
            }
        });

        // ---- 3. FOOTER (Minimalist line) ----
        JPanel footerLine = new JPanel();
        footerLine.setPreferredSize(new Dimension(0, 1));
        footerLine.setBackground(new Color(50, 55, 60));

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
        add(footerLine, BorderLayout.SOUTH);
    }

    private String navItem(String icon, String title) {
        return "<html><body style='width: 130px; padding: 8px;'>"
                + "<span style='font-family: \"Segoe UI Emoji\"; font-size: 16px;'>" + icon + "</span>"
                + "&nbsp;&nbsp;<span style='font-family: \"Segoe UI\";'>" + title + "</span>"
                + "</body></html>";
    }

    private void aksiLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Keluar dari sesi ini?", "Logout", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            this.dispose();
            MainFrame.main(null);
        }
    }

    public static void main(String[] args) {
        try {
            com.formdev.flatlaf.FlatDarkLaf.setup();
            UIManager.put("Button.arc", 15);
            UIManager.put("Component.arc", 15);
            UIManager.put("TabbedPane.selectedBackground", new Color(45, 55, 65));
        } catch (Exception e) {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ex) {}
        }

        if (DBConnection.getConnection() == null) {
            JOptionPane.showMessageDialog(null, "Database Connection Error!");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            // ==========================================
            // MEMBUAT SPLASH SCREEN (LOADING)
            // ==========================================
            JWindow splash = new JWindow();
            JPanel splashPanel = new JPanel(new BorderLayout());
            splashPanel.setBackground(new Color(25, 30, 35));
            splashPanel.setBorder(BorderFactory.createLineBorder(new Color(52, 152, 219), 2)); // Border biru
            
            JLabel lblJudul = new JLabel("BERKAH JAYA SYSTEM", SwingConstants.CENTER);
            lblJudul.setFont(new Font("Segoe UI", Font.BOLD, 28));
            lblJudul.setForeground(Color.WHITE);
            lblJudul.setBorder(new EmptyBorder(20, 0, 10, 0));
            splashPanel.add(lblJudul, BorderLayout.NORTH);

            try {
                URL splashUrl = MainFrame.class.getResource("/resources/bek.jpeg");
                if (splashUrl != null) {
                    Image splashImg = new ImageIcon(splashUrl).getImage().getScaledInstance(500, 350, Image.SCALE_SMOOTH);
                    splashPanel.add(new JLabel(new ImageIcon(splashImg)), BorderLayout.CENTER);
                }
            } catch (Exception e) {}

            JLabel lblLoading = new JLabel("Memuat sistem database...", SwingConstants.CENTER);
            lblLoading.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblLoading.setForeground(Color.LIGHT_GRAY);
            lblLoading.setBorder(new EmptyBorder(10, 0, 20, 0));
            splashPanel.add(lblLoading, BorderLayout.SOUTH);

            splash.getContentPane().add(splashPanel);
            splash.setSize(450, 400);
            splash.setLocationRelativeTo(null);
            splash.setVisible(true);

            // ==========================================
            // TIMER UNTUK MENUTUP SPLASH & BUKA LOGIN
            // ==========================================
            new Thread(() -> {
                try {
                    Thread.sleep(900); // Tahan loading
                } catch (InterruptedException e) {}
                
                SwingUtilities.invokeLater(() -> {
                    splash.dispose(); // Tutup loading
                    
                    // Buka layar Login Split-Screen
                    FormLogin login = new FormLogin();
                    login.setVisible(true);
                    
                    if (login.getIdUserSession() != -1) {
                        new MainFrame(login.getIdUserSession(), login.getNamaUserAktif(), login.getLevelUserSession()); 
                    } else {
                        System.exit(0);     
                    }
                });
            }).start();
        }); 
    } 
} 