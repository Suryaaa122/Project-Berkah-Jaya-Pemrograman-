package berkahjaya.ui;

import berkahjaya.dao.BarangDAO;
import berkahjaya.dao.KategoriDAO; 
import berkahjaya.model.Barang;
import berkahjaya.model.Kategori; 

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.Locale;

public class FormBarang extends JPanel {

    private final BarangDAO barangDAO = new BarangDAO();
    private final KategoriDAO kategoriDAO = new KategoriDAO(); 
    private final NumberFormat rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private JTextField tfId, tfNama, tfHarga, tfStok, tfCari;
    private JComboBox<String> cmbKategori, cmbSatuan; 
    
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;

    private boolean modeEdit = false;

    public FormBarang() {
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
            new LineBorder(new Color(26, 95, 122), 1, true),
            "  Data Barang  ", TitledBorder.LEFT, TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 13), new Color(26, 95, 122)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        Font lf = new Font("Segoe UI", Font.BOLD, 12);
        Font tf = new Font("Segoe UI", Font.PLAIN, 12);

        // Inisialisasi Field
        tfId      = field(tf); 
        tfNama    = field(tf); 
        tfHarga   = field(tf); 
        tfStok    = field(tf); 
        
        // --- VALIDASI NAMA BARANG ---
        tfNama.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetterOrDigit(c) && !Character.isSpaceChar(c) && 
                    c != '-' && c != '.' && c != '(' && c != ')') {
                    e.consume(); 
                }
            }
        });

        // --- VALIDASI HARGA & STOK ---
        KeyAdapter hanyaAngka = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); 
                }
            }
        };
        tfHarga.addKeyListener(hanyaAngka);
        tfStok.addKeyListener(hanyaAngka);
        
        // --- KATEGORI + TOMBOL [+] ---
        cmbKategori = new JComboBox<>();
        cmbKategori.setFont(tf);
        loadKategoriToCombo(); 
        
        JButton btnAddKat = new JButton("+");
        btnAddKat.setToolTipText("Tambah Kategori Baru");
        btnAddKat.addActionListener(e -> tambahKategoriCepat());

        JPanel panelKat = new JPanel(new BorderLayout(5, 0));
        panelKat.setOpaque(false);
        panelKat.add(cmbKategori, BorderLayout.CENTER);
        panelKat.add(btnAddKat, BorderLayout.EAST);
        
        // --- SATUAN + TOMBOL [+] ---
        String[] listSatuan = {"Botol", "Buah", "Dus", "Karung", "Kg", "Liter", "Pack", "Pcs"};
        cmbSatuan = new JComboBox<>(new DefaultComboBoxModel<>(listSatuan));
        cmbSatuan.setFont(tf);

        JButton btnAddSatuan = new JButton("+");
        btnAddSatuan.setToolTipText("Tambah Satuan Baru");
        btnAddSatuan.addActionListener(e -> tambahSatuanCepat());

        JPanel panelSatuan = new JPanel(new BorderLayout(5, 0));
        panelSatuan.setOpaque(false);
        panelSatuan.add(cmbSatuan, BorderLayout.CENTER);
        panelSatuan.add(btnAddSatuan, BorderLayout.EAST);

        // Grid Input
        addRow(formPanel, g, 0, "ID Barang *", tfId, lf);
        addRow(formPanel, g, 1, "Nama Barang *", tfNama, lf);
        addRow(formPanel, g, 2, "Kategori *", panelKat, lf); // Pakai panel baru
        addRow(formPanel, g, 3, "Satuan", panelSatuan, lf); // Pakai panel baru       
        addRow(formPanel, g, 4, "Harga Jual *", tfHarga, lf);
        addRow(formPanel, g, 5, "Stok *", tfStok, lf);

        // Tombol aksi
        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        JButton btnSimpan = tombol("Simpan", new Color(26, 95, 122));
        JButton btnHapus  = tombol("Hapus",  new Color(180, 60, 60));
        JButton btnBatal  = tombol("Ulang",  new Color(100, 100, 100));
        
        btnSimpan.addActionListener(e -> simpan());
        btnHapus.addActionListener(e -> hapus());
        btnBatal.addActionListener(e -> bersihkan());
        
        btnP.add(btnSimpan); btnP.add(btnHapus); btnP.add(btnBatal);
        g.gridx = 0; g.gridy = 6; g.gridwidth = 2;
        formPanel.add(btnP, g);

        // ======= Panel Atas (Form + Search) =======
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setOpaque(false);
        searchPanel.add(new JLabel("Cari Barang: "));
        tfCari = new JTextField(20);
        tfCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = tfCari.getText();
                if (text.trim().length() == 0) {
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
        String[] cols = {"ID Barang","Nama Barang","ID Kat","Satuan","Harga Jual","Stok"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) {
                if (c == 2 || c == 5) return Integer.class;
                return Object.class;
            }
        };
        
        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        
        setupTableUI();

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onTableClick(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(200, 220, 240), 1));

        add(topContainer, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void setupTableUI() {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(26, 95, 122));
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        table.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent comp, Font lf) {
        g.gridwidth = 1; g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel l = new JLabel(label); l.setFont(lf); 
        l.setPreferredSize(new Dimension(110, 26));
        p.add(l, g);
        g.gridx = 1; g.weightx = 1;
        p.add(comp, g);
    }

    private JTextField field(Font f) {
        JTextField t = new JTextField(20); t.setFont(f); return t;
    }

    private JButton tombol(String text, Color bg) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bg.darker(), 1),
            new EmptyBorder(6, 15, 6, 15)
        ));
        return b;
    }

    void loadData() {
        tableModel.setRowCount(0);
        for (Barang b : barangDAO.getAll()) {
            tableModel.addRow(new Object[]{
                b.getIdBarang(), b.getNamaBarang(), b.getIdKategori(),
                b.getSatuan(), rupiahFmt.format(b.getHargaJual()), b.getStok()
            });
        }
    }

    public void loadKategoriToCombo() {
        cmbKategori.removeAllItems();
        for (Kategori k : kategoriDAO.getAll()) {
            cmbKategori.addItem(k.getIdKategori() + " - " + k.getNamaKategori());
        }
    }

    private void onTableClick() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        
        int modelRow = table.convertRowIndexToModel(row);
        String id = tableModel.getValueAt(modelRow, 0).toString();
        Barang b = barangDAO.getById(id);
        
        if (b != null) {
            tfId.setText(b.getIdBarang());
            tfNama.setText(b.getNamaBarang());
            tfHarga.setText(String.valueOf((int)b.getHargaJual()));
            tfStok.setText(String.valueOf(b.getStok()));
            
            for (int i = 0; i < cmbKategori.getItemCount(); i++) {
                if (cmbKategori.getItemAt(i).startsWith(b.getIdKategori() + " -")) {
                    cmbKategori.setSelectedIndex(i);
                    break;
                }
            }
            
            cmbSatuan.setSelectedItem(b.getSatuan());
            
            tfId.setEditable(false);
            tfId.setBackground(new Color(240, 240, 240));
            modeEdit = true;
        }
    }

    private void simpan() {
        try {
            String id = tfId.getText().trim();
            String nama = tfNama.getText().trim();
            
            if (id.isEmpty() || nama.isEmpty()) {
                throw new IllegalArgumentException("ID dan Nama wajib diisi!");
            }
            
            if (cmbKategori.getSelectedItem() == null) {
                throw new IllegalArgumentException("Kategori belum diisi!");
            }
            
            String selectedKat = cmbKategori.getSelectedItem().toString();
            int katId = Integer.parseInt(selectedKat.split(" ")[0]); 
            
            String satuan = cmbSatuan.getSelectedItem().toString();
            double harga = Double.parseDouble(tfHarga.getText().trim());
            int stok = Integer.parseInt(tfStok.getText().trim());

            Barang b = new Barang(id, katId, nama, satuan, harga, stok);
            boolean ok = modeEdit ? barangDAO.update(b) : barangDAO.insert(b);

            if (ok) {
                JOptionPane.showMessageDialog(this, "Data berhasil disimpan!");
                bersihkan();
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal simpan. Cek apakah ID sudah ada.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void hapus() {
        String id = tfId.getText().trim();
        if (id.isEmpty() || !modeEdit) { 
            JOptionPane.showMessageDialog(this, "Pilih data dulu!"); 
            return; 
        }
        
        if (JOptionPane.showConfirmDialog(this, "Hapus barang?") == JOptionPane.YES_OPTION) {
            if (barangDAO.delete(id)) {
                bersihkan();
                loadData();
            }
        }
    }

    private void bersihkan() {
        tfId.setText(barangDAO.generateIdBarang());
        tfId.setEditable(false);
        tfId.setBackground(UIManager.getColor("TextField.background")); 
        
        tfNama.setText(""); 
        tfHarga.setText(""); 
        tfStok.setText("");
        
        loadKategoriToCombo();
        
        if (cmbKategori.getItemCount() > 0) cmbKategori.setSelectedIndex(0);
        if (cmbSatuan.getItemCount() > 0) cmbSatuan.setSelectedIndex(0);
        
        modeEdit = false;
        table.clearSelection();
    }

    // --- FITUR TAMBAH KATEGORI CEPAT ---
    private void tambahKategoriCepat() {
        String namaKat = JOptionPane.showInputDialog(this, "Masukkan Nama Kategori Baru:", "Tambah Kategori", JOptionPane.QUESTION_MESSAGE);
        if (namaKat != null && !namaKat.trim().isEmpty()) {
            Kategori k = new Kategori();
            k.setNamaKategori(namaKat.trim());
            if (kategoriDAO.insert(k)) {
                loadKategoriToCombo();
                cmbKategori.setSelectedIndex(cmbKategori.getItemCount() - 1);
            }
        }
    }

    // --- FITUR TAMBAH SATUAN CEPAT ---
    private void tambahSatuanCepat() {
        String satuanBaru = JOptionPane.showInputDialog(this, "Masukkan Nama Satuan Baru:", "Tambah Satuan", JOptionPane.QUESTION_MESSAGE);
        if (satuanBaru != null && !satuanBaru.trim().isEmpty()) {
            DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>) cmbSatuan.getModel();
            model.addElement(satuanBaru.trim());
            cmbSatuan.setSelectedItem(satuanBaru.trim());
        }
    }
}