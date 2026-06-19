package berkahjaya.ui;

import berkahjaya.dao.CustomerDAO;
import berkahjaya.model.Customer;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

public class FormCustomer extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();

    private JTextField tfId, tfNama, tfAlamat, tfTelepon, tfCari;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private boolean modeEdit = false;

    public FormCustomer() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponents();
        loadData();
        bersihkan(); 
    }

    private void initComponents() {
        // ======= Panel Form Input =======
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(52, 152, 219), 1, true),
            "  DATA CUSTOMER  ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(52, 152, 219)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 8, 7, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        Font lf = new Font("Segoe UI", Font.BOLD, 12);
        
        tfId      = new JTextField(20); 
        tfNama    = new JTextField(20); 

        // --- VALIDASI NAMA (Huruf & Simbol Nama Wajar) ---
        tfNama.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && !Character.isSpaceChar(c) && 
                    c != '\'' && c != '.' && c != ',') {
                    e.consume(); 
                }
            }
        });

        tfAlamat  = new JTextField(20); 
        tfTelepon = new JTextField(20); 

        // --- VALIDASI TELEPON (Hanya Angka) ---
        tfTelepon.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); 
                }
            }
        });

        addRow(formPanel, g, 0, "ID Customer *",  tfId,      lf);
        addRow(formPanel, g, 1, "Nama Customer *", tfNama,    lf);
        addRow(formPanel, g, 2, "Alamat",          tfAlamat,  lf);
        addRow(formPanel, g, 3, "No. Telepon",    tfTelepon, lf);

        // Tombol aksi
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        btnP.setOpaque(false);
        
        JButton btnSimpan = tombol("Simpan", new Color(26, 95, 122));
        JButton btnHapus  = tombol("Hapus",  new Color(180, 60, 60));
        JButton btnBatal  = tombol("Ulang",  new Color(80, 80, 80));
        
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> bersihkan());
        
        btnP.add(btnSimpan); btnP.add(btnHapus); btnP.add(btnBatal);
        g.gridx = 0; g.gridy = 4; g.gridwidth = 2;
        formPanel.add(btnP, g);

        // ======= Panel Pencarian =======
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Cari Customer: "));
        tfCari = new JTextField(20);
        tfCari.putClientProperty("JTextField.placeholderText", "Ketik nama/ID...");
        tfCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = tfCari.getText();
                if (text.trim().isEmpty()) {
                    rowSorter.setRowFilter(null);
                } else {
                    rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        searchPanel.add(tfCari);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(formPanel, BorderLayout.WEST);
        topContainer.add(searchPanel, BorderLayout.SOUTH);

        // ======= Tabel =======
        String[] cols = {"ID Customer", "Nama Customer", "Alamat", "Telepon"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        setupTableUI();

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onTableClick(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        add(topContainer, BorderLayout.NORTH);
        add(scroll,       BorderLayout.CENTER);
    }

    private void setupTableUI() {
        table.setRowHeight(30);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JTextField tf, Font lf) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label); 
        l.setFont(lf); 
        l.setForeground(new Color(180, 185, 190));
        l.setPreferredSize(new Dimension(130, 26));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(tf, g);
    }

    private JButton tombol(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public void loadData() {
        tableModel.setRowCount(0);
        for (Customer c : customerDAO.getAll()) {
            tableModel.addRow(new Object[]{
                c.getIdCustomer(), 
                c.getNamaCustomer(), 
                c.getAlamat(), 
                c.getTelepon()
            });
        }
    }

    private void onTableClick() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int modelRow = table.convertRowIndexToModel(row);
        
        tfId.setText(tableModel.getValueAt(modelRow, 0).toString());
        tfNama.setText(tableModel.getValueAt(modelRow, 1).toString());
        tfAlamat.setText(tableModel.getValueAt(modelRow, 2) != null ? tableModel.getValueAt(modelRow, 2).toString() : "");
        tfTelepon.setText(tableModel.getValueAt(modelRow, 3) != null ? tableModel.getValueAt(modelRow, 3).toString() : "");
        
        tfId.setEditable(false);
        modeEdit = true;
    }

    private void simpan() {
        String id     = tfId.getText().trim();
        String nama   = tfNama.getText().trim();
        String alamat = tfAlamat.getText().trim();
        String telp   = tfTelepon.getText().trim();

        if (id.isEmpty() || nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID dan Nama Customer wajib diisi!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (nama.length() > 50) {
            JOptionPane.showMessageDialog(this, "Nama Customer terlalu panjang! Maksimal 50 karakter.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (alamat.length() > 150) {
            JOptionPane.showMessageDialog(this, "Alamat terlalu panjang! Maksimal 150 karakter.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (telp.length() > 15) {
            JOptionPane.showMessageDialog(this, "No. Telepon terlalu panjang! Maksimal 15 karakter.", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer c = new Customer(id, nama, alamat, telp);
        boolean ok = modeEdit ? customerDAO.update(c) : customerDAO.insert(c);
        
        if (ok) {
            JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
            bersihkan(); 
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data ke database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        String id = tfId.getText().trim();
        if (id.isEmpty()) return;
        
        int confirm = JOptionPane.showConfirmDialog(this, "Hapus customer " + id + "?", "Hapus", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (customerDAO.delete(id)) {
                JOptionPane.showMessageDialog(this, "Data dihapus!");
                bersihkan(); 
                loadData();
            }
        }
    }

    public void bersihkan() {
        tfId.setText(customerDAO.generateIdCustomer()); 
        tfId.setEditable(false); 
        
        tfNama.setText(""); 
        tfAlamat.setText(""); 
        tfTelepon.setText("");
        
        modeEdit = false;
        table.clearSelection();
        if(tfCari != null) tfCari.setText("");
        if(rowSorter != null) rowSorter.setRowFilter(null);
    }
}