package berkahjaya.ui;

import berkahjaya.dao.UserDAO;
import berkahjaya.model.User;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class FormUser extends JPanel {

    private final UserDAO userDAO = new UserDAO();

    private JTextField tfId, tfUsername, tfNama, tfCari;
    private JPasswordField pfPassword;
    private JComboBox<String> cmbLevel; // Menggunakan cmbLevel untuk Admin/Petugas
    
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private boolean modeEdit = false;

    public FormUser() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 20, 15, 20));
        initComponents();
        loadData();
        bersihkan();
    }

    private void initComponents() {
        // ======= 1. PANEL INPUT FORM =======
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(new Color(52, 152, 219), 1, true),
            "  DATA USER APLIKASI  ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), Color.WHITE
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Komponen Input
        tfId = new JTextField(15);
        tfId.setEditable(false);
        tfId.setBackground(new Color(50, 55, 60));
        
        tfUsername = new JTextField(15);
        pfPassword = new JPasswordField(15);
        tfNama = new JTextField(15);
        
        // Pilihan sesuai database kamu
        cmbLevel = new JComboBox<>(new String[]{"Petugas", "Admin"}); 

        // Menambahkan ke GridBagLayout
        addFormRow(formPanel, "ID User (Otomatis)", tfId, gbc, 0);
        addFormRow(formPanel, "Username Login", tfUsername, gbc, 1);
        addFormRow(formPanel, "Password", pfPassword, gbc, 2);
        addFormRow(formPanel, "Nama Lengkap", tfNama, gbc, 3);
        addFormRow(formPanel, "Level Akses", cmbLevel, gbc, 4);

        // ======= 2. PANEL BUTTONS =======
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton btnSimpan = new JButton("💾 Simpan");
        JButton btnHapus = new JButton("🗑 Hapus");
        JButton btnBersih = new JButton("🔄 Bersihkan");

        // Styling Buttons
        btnSimpan.setBackground(new Color(46, 204, 113)); btnSimpan.setForeground(Color.WHITE);
        btnHapus.setBackground(new Color(231, 76, 60)); btnHapus.setForeground(Color.WHITE);
        
        btnPanel.add(btnSimpan);
        btnPanel.add(btnHapus);
        btnPanel.add(btnBersih);

        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        // ======= 3. PANEL TABEL & PENCARIAN =======
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        
        // Bar Pencarian
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        tfCari = new JTextField();
        searchPanel.add(new JLabel("🔍 Cari User: "), BorderLayout.WEST);
        searchPanel.add(tfCari, BorderLayout.CENTER);
        rightPanel.add(searchPanel, BorderLayout.NORTH);

        // Setting JTable
        String[] columns = {"ID User", "Username", "Nama Lengkap", "Level"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(table);
        rightPanel.add(scrollPane, BorderLayout.CENTER);

        // Gabungkan ke Panel Utama (Kiri Form, Kanan Tabel)
        add(formPanel, BorderLayout.WEST);
        add(rightPanel, BorderLayout.CENTER);

        // ======= 4. EVENT LISTENERS =======
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBersih.addActionListener(e -> bersihkan());

        // Klik baris tabel untuk edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    tfId.setText(tableModel.getValueAt(modelRow, 0).toString());
                    tfUsername.setText(tableModel.getValueAt(modelRow, 1).toString());
                    tfNama.setText(tableModel.getValueAt(modelRow, 2).toString());
                    cmbLevel.setSelectedItem(tableModel.getValueAt(modelRow, 3).toString());
                    pfPassword.setText(""); // Kosongkan demi keamanan
                    
                    modeEdit = true;
                }
            }
        });

        // Filter Pencarian Otomatis
        tfCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = tfCari.getText().trim();
                if (text.isEmpty()) rowSorter.setRowFilter(null);
                else rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    private void addFormRow(JPanel panel, String label, JComponent comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setForeground(Color.WHITE);
        panel.add(lbl, gbc);
        
        gbc.gridx = 1; gbc.gridy = row;
        panel.add(comp, gbc);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getIdUser(),
                u.getUsername(),
                u.getNamaLengkap(),
                u.getLevel()
            });
        }
    }

   private void simpan() {
        String username = tfUsername.getText().trim();
        String password = new String(pfPassword.getPassword()).trim();
        String nama = tfNama.getText().trim();
        String level = cmbLevel.getSelectedItem().toString();

        // 1. Cek apakah ada kolom yang belum diisi
        if (username.isEmpty() || nama.isEmpty() || (!modeEdit && password.isEmpty())) {
            JOptionPane.showMessageDialog(this, "Semua data wajib diisi!", "Validasi Gagal", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // [BARU] 2. Validasi Nama Lengkap hanya boleh huruf dan spasi
        if (!nama.matches("^[a-zA-Z\\s]+$")) {
            JOptionPane.showMessageDialog(this, "Nama Lengkap hanya boleh berisi huruf dan spasi!", "Format Salah", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Jika lolos validasi, masukkan data ke model
        User u = new User();
        u.setUsername(username);
        u.setNamaLengkap(nama);
        u.setLevel(level);
        u.setPassword(password);

        boolean sukses;
        if (modeEdit) {
            u.setIdUser(Integer.parseInt(tfId.getText()));
            if (password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Masukkan password untuk konfirmasi perubahan.", "Peringatan", JOptionPane.WARNING_MESSAGE);
                return;
            }
            sukses = userDAO.update(u);
        } else {
            sukses = userDAO.insert(u);
        }

        if (sukses) {
            JOptionPane.showMessageDialog(this, "Data User berhasil disimpan!");
            bersihkan();
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data! Periksa apakah username duplikat.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapus() {
        String idText = tfId.getText();
        if (idText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih user dari tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idUser = Integer.parseInt(idText);
        if (idUser == 1) {
            JOptionPane.showMessageDialog(this, "Administrator Utama tidak boleh dihapus!", "Ditolak", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Hapus user ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.delete(idUser)) {
                JOptionPane.showMessageDialog(this, "User berhasil dihapus.");
                bersihkan();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal menghapus user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void bersihkan() {
        tfId.setText("");
        tfUsername.setText("");
        pfPassword.setText("");
        tfNama.setText("");
        cmbLevel.setSelectedIndex(0);
        table.clearSelection();
        modeEdit = false;
    }
}