package berkahjaya.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Vector;

public class FormLogSession extends JPanel {
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnRefresh;

    public FormLogSession() {
        initComponents();
        loadLogData();
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(new Color(30, 35, 40)); // Menyesuaikan tema FlatDark

        // ---- HEADER PANEL ----
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("Log Sesi Login Kasir (Per Hari)");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);
        headerPanel.add(lblTitle, BorderLayout.WEST);

        btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadLogData());
        headerPanel.add(btnRefresh, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // ---- TABLE PANEL ----
        tableModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 55, 60)));
        add(scrollPane, BorderLayout.CENTER);
    }

    public void loadLogData() {
        // 1. Set Header Kolom
        Vector<String> header = new Vector<>();
        header.add("Waktu Login");
        header.add("ID Kasir");
        header.add("Nama Kasir");
        header.add("Nama PC / Device");

        // 2. Ambil data dari backend SysCache yang sudah kita buat sebelumnya
        java.util.List<Vector<Object>> data = sec.SysCache.getSessionLog();

        // 3. Masukkan ke dalam tabel
        tableModel.setDataVector(new Vector<>(data), header);
    }
}