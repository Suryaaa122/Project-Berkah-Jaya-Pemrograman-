package berkahjaya.ui;

import berkahjaya.dao.BarangDAO;
import berkahjaya.dao.CustomerDAO;
import berkahjaya.dao.PenjualanDAO;
import berkahjaya.model.Barang;
import berkahjaya.model.Customer;
import berkahjaya.model.Penjualan;         
import berkahjaya.model.DetailPenjualan;   
import berkahjaya.ui.Settings; 
import java.util.ArrayList;            
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FormTransaksi extends JPanel {

    private final CustomerDAO customerDAO = new CustomerDAO();
    private final BarangDAO barangDAO = new BarangDAO();
    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final int idUserLogin;
    private final String namaUserLogin;

    private JComboBox<Customer> cmbCustomer;
    private JComboBox<Barang> cmbBarang;
    private JTextField tfHarga, tfStok, tfJumlah, tfSubTotal, tfTanggal;
    private JButton btnTambahKeranjang, btnBatal;

    private JPanel panelKeranjang;
    private JScrollPane scrollKeranjang;
    private List<ItemKeranjang> listItemKeranjang = new ArrayList<>();
    
    private JLabel lblGrandTotal;
    private JTextField tfBayar, tfKembali;
    private JButton btnBayar;

    private JLabel lblPpnPajak;
    private JTextField tfPpnPajak;

    private double grandTotal = 0.0;
    private double totalPajak = 0.0; 
    private String currentCustomerId = null;
    private final NumberFormat rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private class ItemKeranjang {
        String idBarang;
        String namaBarang;
        double hargaSatuan;
        int qty;
        double subTotal;
        
        public ItemKeranjang(String idBarang, String namaBarang, double hargaSatuan, int qty) {
            this.idBarang = idBarang;
            this.namaBarang = namaBarang;
            this.hargaSatuan = hargaSatuan;
            this.qty = qty;
            this.subTotal = hargaSatuan * qty;
        }
    }

    public FormTransaksi(int idUserLogin, String namaUserLogin) {
        this.idUserLogin = idUserLogin;
        this.namaUserLogin = namaUserLogin;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setOpaque(false);

        mainPanel.add(createFormPanel(), BorderLayout.NORTH);

        // --- PANEL KERANJANG BELANJA ---
        JPanel keranjangContainer = new JPanel(new BorderLayout());
        keranjangContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 180)),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));
        
        JPanel headerKeranjang = new JPanel(new BorderLayout());
        headerKeranjang.setBackground(new Color(52, 73, 94));
        headerKeranjang.setPreferredSize(new Dimension(0, 40));
        headerKeranjang.setBorder(new EmptyBorder(0, 15, 0, 15));
        
        JLabel lblHeaderKiri = new JLabel("PRODUK");
        lblHeaderKiri.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderKiri.setForeground(Color.WHITE);
        
        JPanel headerKanan = new JPanel(new GridLayout(1, 4, 20, 0));
        headerKanan.setOpaque(false);
        headerKanan.setPreferredSize(new Dimension(500, 40));
        String[] headers = {"HARGA", "QTY", "SUBTOTAL", "AKSI"};
        for (String h : headers) {
            JLabel lbl = new JLabel(h, SwingConstants.CENTER);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lbl.setForeground(Color.WHITE);
            headerKanan.add(lbl);
        }
        
        headerKeranjang.add(lblHeaderKiri, BorderLayout.WEST);
        headerKeranjang.add(headerKanan, BorderLayout.EAST);
        
        panelKeranjang = new JPanel();
        panelKeranjang.setLayout(new BoxLayout(panelKeranjang, BoxLayout.Y_AXIS));
        panelKeranjang.setBackground(new Color(34, 47, 62));
        
        scrollKeranjang = new JScrollPane(panelKeranjang);
        scrollKeranjang.setPreferredSize(new Dimension(0, 410)); 
        scrollKeranjang.setBorder(null);
        scrollKeranjang.getViewport().setBackground(new Color(34, 47, 62));
        
        keranjangContainer.add(headerKeranjang, BorderLayout.NORTH);
        keranjangContainer.add(scrollKeranjang, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(new Color(46, 204, 113));
        infoPanel.setBorder(new EmptyBorder(5, 15, 5, 15));
        
        // FIX: Inject font emoji via HTML agar gambar orang muncul riil
        JLabel lblInfoCustomer = new JLabel("<html><font face='Segoe UI Emoji'>👤</font> Customer: -</html>"); 
        lblInfoCustomer.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblInfoCustomer.setForeground(Color.WHITE);
        lblInfoCustomer.setName("lblInfoCustomer");
        
        JLabel lblShortcutHint = new JLabel("F1:Customer | F2:Produk | F3:Jumlah | F4:Bayar | F8:Hapus Item | F12:Bayar | Esc:Reset");
        lblShortcutHint.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        lblShortcutHint.setForeground(new Color(255, 255, 255, 200));
        
        infoPanel.add(lblInfoCustomer, BorderLayout.WEST);
        infoPanel.add(lblShortcutHint, BorderLayout.EAST);
        keranjangContainer.add(infoPanel, BorderLayout.SOUTH);
        
        mainPanel.add(keranjangContainer, BorderLayout.CENTER);
        mainPanel.add(createCheckoutPanel(), BorderLayout.SOUTH);

        add(mainPanel, BorderLayout.CENTER);
        
        // === SHORTCUT KEYBOARD ===
        bindKey(KeyEvent.VK_F1, () -> cmbCustomer.requestFocus());
        bindKey(KeyEvent.VK_F2, () -> { cmbBarang.requestFocus(); cmbBarang.showPopup(); });
        bindKey(KeyEvent.VK_F3, () -> { tfJumlah.requestFocus(); tfJumlah.selectAll(); });
        bindKey(KeyEvent.VK_F4, () -> { tfBayar.requestFocus(); tfBayar.selectAll(); });
        bindKey(KeyEvent.VK_F12, () -> prosesPembayaran());
        bindKey(KeyEvent.VK_ESCAPE, () -> bersihkanFormInput());
        bindKey(KeyEvent.VK_F8, () -> { if (!listItemKeranjang.isEmpty()) hapusItem(listItemKeranjang.size() - 1); });
        
        tfJumlah.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { 
                hitungSubTotal(); 
                if(e.getKeyCode() == KeyEvent.VK_ENTER) tambahKeKeranjang();
            }
        });
        
        tfBayar.addActionListener(e -> prosesPembayaran());
        
        tampilkanPesanKosong();
        if (cmbBarang.getItemCount() > 0) onBarangSelected();
    }

    private void bindKey(int keyCode, Runnable action) {
        String key = "key_" + keyCode;
        this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), key);
        this.getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { action.run(); }
        });
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        Font gblFont = new Font("Segoe UI", Font.BOLD, 13);

        // ================= KOLOM 1 (KIRI) =================
        gbc.weightx = 1.0;
        
        tfTanggal = buatField(false);
        tfTanggal.setText(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
        addRow(formPanel, gbc, 0, 0, "WAKTU", tfTanggal, gblFont);

        cmbCustomer = new JComboBox<>();
        cmbCustomer.setEditable(true);
        loadCustomers();
        setupAutoComplete(cmbCustomer);
        cmbCustomer.addActionListener(e -> cekGantiCustomer());
        addRow(formPanel, gbc, 0, 1, "CUSTOMER (F1)", cmbCustomer, gblFont);

        cmbBarang = new JComboBox<>();
        cmbBarang.setEditable(true);
        loadBarang();
        setupAutoComplete(cmbBarang);
        cmbBarang.addActionListener(e -> onBarangSelected());
        addRow(formPanel, gbc, 0, 2, "PRODUK (F2)", cmbBarang, gblFont);

        // ================= KOLOM 2 (KANAN) =================
        tfHarga = buatField(false);
        addRow(formPanel, gbc, 1, 0, "HARGA SATUAN", tfHarga, gblFont);

        tfStok = buatField(false);
        addRow(formPanel, gbc, 1, 1, "STOK TERSEDIA", tfStok, gblFont);

        tfJumlah = buatField(true);
        tfJumlah.setText("1");
        ((AbstractDocument) tfJumlah.getDocument()).setDocumentFilter(new NumericFilter());
        addRow(formPanel, gbc, 1, 2, "JUMLAH BELI (F3)", tfJumlah, gblFont);

        tfSubTotal = buatField(false);
        tfSubTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tfSubTotal.setForeground(new Color(46, 204, 113)); 
        addRow(formPanel, gbc, 1, 3, "SUB TOTAL", tfSubTotal, gblFont);

        // ================= ROW TOMBOL (SOLUSI FIX: EMOJI MUNCUL & ANTI PATAH) =================
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        btnPanel.setOpaque(false);

        // Tombol 1: Tambah Keranjang
        btnTambahKeranjang = new JButton("➕ TAMBAH KE KERANJANG (Enter)");
        btnTambahKeranjang.setBackground(new Color(46, 204, 113));
        btnTambahKeranjang.setForeground(Color.WHITE);
        // KUNCI: Ganti ke font "Dialog" biar emojinya tembus muncul, tapi teksnya gak wrap!
        btnTambahKeranjang.setFont(new Font("Dialog", Font.BOLD, 12)); 
        btnTambahKeranjang.setFocusPainted(false);
        btnTambahKeranjang.setPreferredSize(new Dimension(260, 38)); 
        btnTambahKeranjang.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTambahKeranjang.addActionListener(e -> tambahKeKeranjang());

        // Tombol 2: Reset Form
        btnBatal = new JButton("🔄 RESET FORM (Esc)");
        // KUNCI: Samakan pakai font "Dialog" biar aman dari kotak tahu dan tetep sejajar lurus!
        btnBatal.setFont(new Font("Dialog", Font.PLAIN, 12)); 
        btnBatal.setPreferredSize(new Dimension(180, 38)); 
        btnBatal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBatal.addActionListener(e -> bersihkanFormInput());

        btnPanel.add(btnTambahKeranjang);
        btnPanel.add(btnBatal);

        // Gembok posisi di GridBag Layout
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.NONE;       
        gbc.anchor = GridBagConstraints.CENTER;   
        gbc.insets = new Insets(25, 10, 5, 10);
        formPanel.add(btnPanel, gbc);

        return formPanel;
    }

    private void setupAutoComplete(JComboBox<?> comboBox) {
        JTextField editor = (JTextField) comboBox.getEditor().getEditorComponent();
        
        comboBox.getModel().addListDataListener(new javax.swing.event.ListDataListener() {
            @Override public void intervalAdded(javax.swing.event.ListDataEvent e) { updateMaster(); }
            @Override public void intervalRemoved(javax.swing.event.ListDataEvent e) { updateMaster(); }
            @Override public void contentsChanged(javax.swing.event.ListDataEvent e) { updateMaster(); }
            
            private void updateMaster() {
                Boolean isFiltering = (Boolean) comboBox.getClientProperty("IS_FILTERING");
                if (isFiltering != null && isFiltering) return;
                
                SwingUtilities.invokeLater(() -> {
                    Boolean filtering = (Boolean) comboBox.getClientProperty("IS_FILTERING");
                    if (filtering != null && filtering) return;
                    
                    List<Object> masterList = new ArrayList<>();
                    for (int i = 0; i < comboBox.getItemCount(); i++) {
                        masterList.add(comboBox.getItemAt(i));
                    }
                    comboBox.putClientProperty("MASTER_LIST", masterList);
                });
            }
        });

        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_UP || code == KeyEvent.VK_DOWN ||
                    code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT ||
                    code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB || 
                    code == KeyEvent.VK_ESCAPE) return;
                
                String typed = editor.getText();
                int caretPos = editor.getCaretPosition();
                
                @SuppressWarnings("unchecked")
                List<Object> masterList = (List<Object>) comboBox.getClientProperty("MASTER_LIST");
                if (masterList == null || masterList.isEmpty()) return;
                
                comboBox.putClientProperty("IS_FILTERING", true);
                
                ActionListener[] listeners = comboBox.getActionListeners();
                for (ActionListener l : listeners) comboBox.removeActionListener(l);
                
                @SuppressWarnings("unchecked")
                DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) comboBox.getModel();
                model.removeAllElements();
                
                for (Object item : masterList) {
                    if (item == null) {
                        if (typed.isEmpty()) model.addElement(null);
                        continue;
                    }
                    if (item.toString().toLowerCase().contains(typed.toLowerCase())) {
                        model.addElement(item);
                    }
                }
                
                editor.setText(typed); 
                editor.setCaretPosition(caretPos); 
                
                for (ActionListener l : listeners) comboBox.addActionListener(l);
                comboBox.putClientProperty("IS_FILTERING", false);
                
                if (model.getSize() > 0) {
                    comboBox.showPopup(); 
                } else {
                    comboBox.hidePopup();
                }
            }
        });

        comboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
            
            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {
                @SuppressWarnings("unchecked")
                List<Object> masterList = (List<Object>) comboBox.getClientProperty("MASTER_LIST");
                if (masterList == null) return;
                
                comboBox.putClientProperty("IS_FILTERING", true);
                Object selectedItem = comboBox.getSelectedItem();
                
                ActionListener[] listeners = comboBox.getActionListeners();
                for (ActionListener l : listeners) comboBox.removeActionListener(l);
                
                @SuppressWarnings("unchecked")
                DefaultComboBoxModel<Object> model = (DefaultComboBoxModel<Object>) comboBox.getModel();
                model.removeAllElements();
                for (Object item : masterList) {
                    model.addElement(item);
                }
                
                comboBox.setSelectedItem(selectedItem);
                for (ActionListener l : listeners) comboBox.addActionListener(l);
                comboBox.putClientProperty("IS_FILTERING", false);
            }
        });
    }

    private void tampilkanPesanKosong() {
        panelKeranjang.removeAll();
        JPanel emptyPanel = new JPanel(new GridBagLayout());
        emptyPanel.setBackground(new Color(34, 47, 62));
        emptyPanel.setPreferredSize(new Dimension(100, 200));
        JLabel lblKosong = new JLabel("Keranjang masih kosong - Silakan tambahkan produk");
        lblKosong.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblKosong.setForeground(new Color(170, 175, 180));
        emptyPanel.add(lblKosong);
        panelKeranjang.add(emptyPanel);
        panelKeranjang.revalidate();
        panelKeranjang.repaint();
    }

    private JPanel createItemRow(ItemKeranjang item, int index) {
        JPanel rowPanel = new JPanel(new BorderLayout(10, 0));
        rowPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(52, 73, 94)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        rowPanel.setBackground(new Color(44, 62, 80));
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        
        JLabel lblNama = new JLabel(item.namaBarang);
        lblNama.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNama.setForeground(Color.WHITE);
        lblNama.setPreferredSize(new Dimension(200, 25));
        
        JPanel rightPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        rightPanel.setOpaque(false);
        
        JLabel lblHarga = new JLabel(rupiahFmt.format(item.hargaSatuan), SwingConstants.CENTER);
        lblHarga.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblHarga.setForeground(new Color(220, 225, 230));
        
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        qtyPanel.setOpaque(false);
        JButton btnMinus = createSmallButton("−", new Color(231, 76, 60), e -> updateQtyItem(index, -1));
        JLabel lblQty = new JLabel(String.valueOf(item.qty), SwingConstants.CENTER);
        lblQty.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblQty.setForeground(Color.WHITE);
        lblQty.setPreferredSize(new Dimension(30, 25));
        JButton btnPlus = createSmallButton("+", new Color(46, 204, 113), e -> updateQtyItem(index, 1));
        qtyPanel.add(btnMinus); qtyPanel.add(lblQty); qtyPanel.add(btnPlus);
        
        JLabel lblSubTotal = new JLabel(rupiahFmt.format(item.subTotal), SwingConstants.CENTER);
        lblSubTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSubTotal.setForeground(new Color(46, 204, 113));
        
        JButton btnHapus = new JButton("X");
        btnHapus.setFont(new Font("Arial", Font.BOLD, 16));
        btnHapus.setForeground(new Color(231, 76, 60));
        btnHapus.setFocusPainted(false); btnHapus.setBorderPainted(false);
        btnHapus.setContentAreaFilled(false);
        btnHapus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnHapus.addActionListener(e -> hapusItem(index));
        
        rightPanel.add(lblHarga); rightPanel.add(qtyPanel);
        rightPanel.add(lblSubTotal); rightPanel.add(btnHapus);
        
        rowPanel.add(lblNama, BorderLayout.WEST);
        rowPanel.add(rightPanel, BorderLayout.EAST);
        return rowPanel;
    }
    
    private JButton createSmallButton(String text, Color bgColor, ActionListener listener) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(bgColor); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(30, 28));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(listener);
        return btn;
    }

    private void refreshPanelKeranjang() {
        panelKeranjang.removeAll();
        if (listItemKeranjang.isEmpty()) {
            tampilkanPesanKosong();
            currentCustomerId = null;
            updateLabelCustomer("-");
        } else {
            for (int i = 0; i < listItemKeranjang.size(); i++) {
                JPanel row = createItemRow(listItemKeranjang.get(i), i);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                panelKeranjang.add(row);
            }
            updateLabelCustomer(getNamaCustomerById(currentCustomerId));
        }
        panelKeranjang.revalidate(); panelKeranjang.repaint();
        updateTampilanTotal();
    }
    
    // FIX: Inject HTML & Font Segoe UI Emoji saat mengupdate nama customer di bar hijau bawah keranjang
    private void updateLabelCustomer(String nama) {
        for (Component c : getComponentsRecursive(this)) {
            if (c instanceof JLabel && "lblInfoCustomer".equals(c.getName())) {
                ((JLabel)c).setText("<html><font face='Segoe UI Emoji'>👤</font> Customer: " + nama + "</html>");
                return;
            }
        }
    }
    
    private List<Component> getComponentsRecursive(Container c) {
        List<Component> list = new ArrayList<>();
        for (Component child : c.getComponents()) {
            list.add(child);
            if (child instanceof Container) list.addAll(getComponentsRecursive((Container)child));
        }
        return list;
    }

    private String getNamaCustomerById(String id) {
        if (id == null) return "-";
        for (int i = 0; i < cmbCustomer.getItemCount(); i++) {
            Customer c = cmbCustomer.getItemAt(i);
            if (c != null && id.equals(c.getIdCustomer())) return c.getNamaCustomer();
        }
        return id;
    }

    private void cekGantiCustomer() {
        Object item = cmbCustomer.getSelectedItem();
        if (!(item instanceof Customer)) return; 

        Customer selected = (Customer) item;
        String newId = selected.getIdCustomer();
        if (newId.equals(currentCustomerId)) return;
        
        if (listItemKeranjang.isEmpty()) {
            currentCustomerId = newId;
            updateLabelCustomer(selected.getNamaCustomer());
            return;
        }
        
        if (currentCustomerId != null && !newId.equals(currentCustomerId)) {
            int p = JOptionPane.showConfirmDialog(this,
                "Keranjang untuk: " + getNamaCustomerById(currentCustomerId) + "\nGanti customer = HAPUS keranjang!\nLanjutkan?",
                "Konfirmasi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (p == JOptionPane.YES_OPTION) {
                resetKeranjang();
                currentCustomerId = newId;
                updateLabelCustomer(selected.getNamaCustomer());
            } else {
                ActionListener[] arr = cmbCustomer.getActionListeners();
                for (ActionListener al : arr) cmbCustomer.removeActionListener(al);
                pilihCustomerById(currentCustomerId);
                cmbCustomer.addActionListener(e -> cekGantiCustomer());
            }
        }
    }
    
    private void pilihCustomerById(String id) {
        if (id == null) { cmbCustomer.setSelectedIndex(0); return; }
        for (int i = 0; i < cmbCustomer.getItemCount(); i++) {
            Customer c = cmbCustomer.getItemAt(i);
            if (c != null && id.equals(c.getIdCustomer())) { cmbCustomer.setSelectedIndex(i); return; }
        }
    }
    
    private void resetKeranjang() {
        listItemKeranjang.clear(); grandTotal = 0; totalPajak = 0; currentCustomerId = null;
        tfBayar.setText(""); tfKembali.setText("");
        refreshPanelKeranjang(); hitungKembalian();
    }

    private void tambahKeKeranjang() {
        Object itemBarang = cmbBarang.getSelectedItem();
        if (!(itemBarang instanceof Barang)) {
            JOptionPane.showMessageDialog(this, "Pilih produk yang valid dari daftar dropdown!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Barang b = (Barang) itemBarang;

        Object itemCustomer = cmbCustomer.getSelectedItem();
        if (!(itemCustomer instanceof Customer)) { 
            JOptionPane.showMessageDialog(this, "Pilih customer yang valid dari daftar dropdown!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return; 
        }
        Customer sel = (Customer) itemCustomer;

        if (currentCustomerId == null) { currentCustomerId = sel.getIdCustomer(); updateLabelCustomer(sel.getNamaCustomer()); }
        if (!sel.getIdCustomer().equals(currentCustomerId)) {
            JOptionPane.showMessageDialog(this, "Customer tidak sesuai!\nSaat ini: " + getNamaCustomerById(currentCustomerId)); return;
        }
        
        try {
            int gty = tfJumlah.getText().isEmpty() ? 0 : Integer.parseInt(tfJumlah.getText());
            Barang bf = barangDAO.getById(b.getIdBarang());
            if (gty <= 0) { JOptionPane.showMessageDialog(this, "Jumlah tidak boleh 0!"); return; }
            
            int diKeranjang = 0;
            for (ItemKeranjang ik : listItemKeranjang)
                if (ik.idBarang.equals(bf.getIdBarang())) diKeranjang += ik.qty;
                
            if (gty + diKeranjang > bf.getStok()) {
                JOptionPane.showMessageDialog(this, "Stok kurang! Sisa: " + (bf.getStok() - diKeranjang)); return;
            }
            
            boolean found = false;
            for (ItemKeranjang ik : listItemKeranjang)
                if (ik.idBarang.equals(bf.getIdBarang())) { ik.qty += gty; ik.subTotal = ik.hargaSatuan * ik.qty; found = true; break; }
                
            if (!found) listItemKeranjang.add(new ItemKeranjang(bf.getIdBarang(), bf.getNamaBarang(), bf.getHargaJual(), gty));
            
            hitungGrandTotal(); refreshPanelKeranjang(); hitungKembalian(); bersihkanFormInput(); tfBayar.requestFocus();
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Input tidak valid!"); 
        }
    }

    private void updateQtyItem(int i, int d) {
        if (i < 0 || i >= listItemKeranjang.size()) return;
        ItemKeranjang ik = listItemKeranjang.get(i);
        int nq = ik.qty + d;
        if (nq <= 0) { hapusItem(i); return; }
        Barang b = barangDAO.getById(ik.idBarang);
        if (b != null && nq > b.getStok()) { JOptionPane.showMessageDialog(this, "Stok maks: " + b.getStok()); return; }
        ik.qty = nq; ik.subTotal = ik.hargaSatuan * ik.qty;
        hitungGrandTotal(); refreshPanelKeranjang(); hitungKembalian();
    }

    private void hapusItem(int i) {
        if (i < 0 || i >= listItemKeranjang.size()) return;
        if (JOptionPane.showConfirmDialog(this, "Hapus " + listItemKeranjang.get(i).namaBarang + "?") == JOptionPane.YES_OPTION) {
            listItemKeranjang.remove(i); hitungGrandTotal(); refreshPanelKeranjang(); hitungKembalian();
        }
    }

    private void hitungGrandTotal() {
        double subTotalKeranjang = 0;
        for (ItemKeranjang ik : listItemKeranjang) {
            subTotalKeranjang += ik.subTotal;
        }
        
        if (Settings.isPakaiPpn()) {
            totalPajak = subTotalKeranjang * 0.11; 
        } else {
            totalPajak = 0.0;
        }
        
        grandTotal = subTotalKeranjang + totalPajak;
    }

    private JPanel createCheckoutPanel() {
        JPanel p = new JPanel(new BorderLayout(20, 10));
        p.setOpaque(false); 
        p.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JPanel gt = new JPanel(new BorderLayout()); 
        gt.setOpaque(false);
        JLabel t = new JLabel("GRAND TOTAL"); 
        t.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        t.setForeground(new Color(150, 155, 160));
        
        lblGrandTotal = new JLabel("Rp0,00"); 
        lblGrandTotal.setFont(new Font("Segoe UI", Font.BOLD, 36)); 
        lblGrandTotal.setForeground(new Color(241, 196, 15));
        
        gt.add(t, BorderLayout.NORTH); 
        gt.add(lblGrandTotal, BorderLayout.CENTER);
        
        JPanel byr = new JPanel(new GridBagLayout()); 
        byr.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints(); 
        g.insets = new Insets(5, 5, 5, 5); 
        g.fill = GridBagConstraints.HORIZONTAL;
        
        Font ff = new Font("Segoe UI", Font.BOLD, 13);
        
        tfBayar = buatField(true); 
        tfBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        ((AbstractDocument)tfBayar.getDocument()).setDocumentFilter(new NumericFilter());
        tfBayar.addKeyListener(new KeyAdapter() { 
            @Override 
            public void keyReleased(KeyEvent e) { 
                hitungKembalian(); 
            } 
        });
        
        tfKembali = buatField(false); 
        tfKembali.setFont(new Font("Segoe UI", Font.BOLD, 16)); 
        tfKembali.setForeground(new Color(46, 204, 113)); 

        lblPpnPajak = new JLabel("PPN (11%):");
        lblPpnPajak.setFont(ff);
        tfPpnPajak = buatField(false);
        tfPpnPajak.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tfPpnPajak.setForeground(new Color(241, 196, 15));
        
        lblPpnPajak.setVisible(Settings.isPakaiPpn());
        tfPpnPajak.setVisible(Settings.isPakaiPpn());
        
        // FIX: Inject HTML & Font Segoe UI Emoji agar icon kartu kredit keluar berwarna riil
        btnBayar = new JButton("<html><font face='Segoe UI Emoji'>💳</font> BAYAR & SELESAI (F12)</html>"); 
        btnBayar.setBackground(new Color(26, 95, 122)); 
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        btnBayar.setFocusPainted(false); 
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.addActionListener(e -> prosesPembayaran());
        
        btnBayar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
            KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "bayarAction"
        );
        btnBayar.getActionMap().put("bayarAction", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prosesPembayaran();
            }
        });
        
        g.gridx=0; g.gridy=0; byr.add(new JLabel("BAYAR (CASH):") {{ setFont(ff); }}, g);
        g.gridx=1; byr.add(tfBayar, g);
        
        g.gridx=0; g.gridy=1; byr.add(lblPpnPajak, g);
        g.gridx=1; byr.add(tfPpnPajak, g);
        
        g.gridx=0; g.gridy=2; byr.add(new JLabel("KEMBALIAN:") {{ setFont(ff); }}, g);
        g.gridx=1; byr.add(tfKembali, g);
        
        g.gridx=0; g.gridy=3; g.gridwidth=2; g.insets = new Insets(15, 5, 5, 5); 
        byr.add(btnBayar, g);
        
        p.add(gt, BorderLayout.WEST); 
        p.add(byr, BorderLayout.EAST);
        
        return p;
    }

    private void updateTampilanTotal() { 
        lblGrandTotal.setText(rupiahFmt.format(grandTotal)); 
        tfPpnPajak.setText(rupiahFmt.format(totalPajak));
    }

    private void hitungKembalian() {
        try {
            double byr = tfBayar.getText().isEmpty() ? 0 : Double.parseDouble(tfBayar.getText());
            double kem = byr - grandTotal;
            if (kem < 0) { tfKembali.setText("Kurang: " + rupiahFmt.format(Math.abs(kem))); tfKembali.setForeground(new Color(231,76,60)); }
            else { tfKembali.setText(rupiahFmt.format(kem)); tfKembali.setForeground(new Color(46,204,113)); }
        } catch (NumberFormatException e) { tfKembali.setText("Rp0,00"); }
    }

    private void prosesPembayaran() {
        if (listItemKeranjang.isEmpty()) { JOptionPane.showMessageDialog(this, "Keranjang kosong!"); return; }
        Customer c = (Customer) cmbCustomer.getSelectedItem();
        if (c == null) { JOptionPane.showMessageDialog(this, "Pilih customer!"); return; }
        if (!c.getIdCustomer().equals(currentCustomerId)) { JOptionPane.showMessageDialog(this, "Customer tidak sesuai!"); return; }
        
        try {
            double byr = tfBayar.getText().isEmpty() ? 0 : Double.parseDouble(tfBayar.getText());
            if (byr < grandTotal) { JOptionPane.showMessageDialog(this, "Uang kurang!"); return; }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Customer: ").append(c.getNamaCustomer()).append("\n\nDetail:\n");
            
            for (ItemKeranjang ik : listItemKeranjang) {
                sb.append("- ").append(ik.namaBarang).append(" (").append(ik.qty).append("x) = ").append(rupiahFmt.format(ik.subTotal)).append("\n");
            }
            
            sb.append("\nTotal: ").append(rupiahFmt.format(grandTotal));
            sb.append("\nBayar: ").append(rupiahFmt.format(byr));
            sb.append("\nKembali: ").append(rupiahFmt.format(byr - grandTotal));
            
            if (JOptionPane.showConfirmDialog(this, sb.toString(), "Konfirmasi Pembayaran", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Penjualan p = new Penjualan(new Date(), c.getIdCustomer(), grandTotal, idUserLogin);
                List<DetailPenjualan> det = new ArrayList<>();
                for (ItemKeranjang ik : listItemKeranjang) {
                    det.add(new DetailPenjualan(0, ik.idBarang, ik.qty, ik.subTotal));
                }
                
                int idNotaResult = penjualanDAO.simpanTransaksi(p, det);

                if (idNotaResult > 0) { 
                    JOptionPane.showMessageDialog(this, "Transaksi Berhasil!");
                    
                    tampilkanStruk(idNotaResult, c.getNamaCustomer(), grandTotal, byr);
                    
                    listItemKeranjang.clear(); 
                    grandTotal = 0; 
                    totalPajak = 0;
                    currentCustomerId = null;
                    tfBayar.setText(""); 
                    tfKembali.setText(""); 
                    refreshPanelKeranjang(); 
                    refreshComboBoxes();
                } else {
                    JOptionPane.showMessageDialog(this, "Transaksi Gagal disimpan ke Database!");
                }
            }
        } catch (NumberFormatException e) { 
            JOptionPane.showMessageDialog(this, "Nominal pembayaran tidak valid! Pastikan hanya angka."); 
        }
    }

    private void hitungSubTotal() {
        Object item = cmbBarang.getSelectedItem();
        if (!(item instanceof Barang)) {
            tfSubTotal.setText("Rp0,00");
            return;
        }
        Barang b = (Barang) item;
        try { tfSubTotal.setText(rupiahFmt.format(b.getHargaJual() * Integer.parseInt(tfJumlah.getText().isEmpty()?"0":tfJumlah.getText()))); }
        catch (NumberFormatException e) { tfSubTotal.setText("Rp0,00"); }
    }

    private JTextField buatField(boolean ed) {
        JTextField tf = new JTextField(20); tf.setEditable(ed); tf.setPreferredSize(new Dimension(0, 33)); return tf;
    }

    private void addRow(JPanel p, GridBagConstraints g, int col, int row, String labelText, JComponent c, Font f) {
        g.gridwidth = 1;
        g.gridy = row; 
        
        g.gridx = col * 2; 
        g.weightx = 0.0;
        g.insets = new Insets(6, col == 0 ? 0 : 30, 6, 10); 
        JLabel lbl = new JLabel(labelText); 
        lbl.setFont(f); 
        lbl.setForeground(new Color(150,155,160)); 
        p.add(lbl, g);
        
        g.gridx = (col * 2) + 1; 
        g.weightx = 1.0;
        g.insets = new Insets(6, 0, 6, 0);
        p.add(c, g);
    }

    private void onBarangSelected() {
        Object item = cmbBarang.getSelectedItem();
        
        if (item instanceof Barang) {
            Barang b = (Barang) item;
            Barang bf = barangDAO.getById(b.getIdBarang());
            tfHarga.setText(rupiahFmt.format(bf.getHargaJual()));
            tfStok.setText(bf.getStok() + " " + bf.getSatuan());
            tfStok.setForeground(bf.getStok() <= 5 ? new Color(231,76,60) : new Color(150,155,160));
            hitungSubTotal();
        } else {
            tfHarga.setText("");
            tfStok.setText("");
            tfSubTotal.setText("Rp0,00");
        }
    }

    public void loadCustomers() {
        cmbCustomer.removeAllItems();
        customerDAO.getAll().forEach(cmbCustomer::addItem);
        cmbCustomer.insertItemAt(null, 0); cmbCustomer.setSelectedIndex(0);
    }

    public void loadBarang() {
        cmbBarang.removeAllItems();
        barangDAO.getAll().forEach(cmbBarang::addItem);
        cmbBarang.insertItemAt(null, 0); 
        cmbBarang.setSelectedIndex(0);
    }

    public void refreshComboBoxes() { loadCustomers(); loadBarang(); if (cmbBarang.getItemCount() > 0) onBarangSelected(); }

    private void bersihkanFormInput() {
        ActionListener[] listeners = cmbBarang.getActionListeners();
        for (ActionListener l : listeners) cmbBarang.removeActionListener(l);
        
        cmbBarang.setSelectedItem(null);
        ((JTextField) cmbBarang.getEditor().getEditorComponent()).setText("");
        
        for (ActionListener l : listeners) cmbBarang.addActionListener(l);
        
        tfHarga.setText("");
        tfStok.setText("");
        tfJumlah.setText("1");
        tfSubTotal.setText("Rp0,00"); 
        
        cmbBarang.requestFocus();
    }

    class NumericFilter extends DocumentFilter {
        @Override public void insertString(FilterBypass fb, int o, String s, AttributeSet a) throws BadLocationException
        { if (s.matches("\\d+")) super.insertString(fb, o, s, a); }
        @Override public void replace(FilterBypass fb, int o, int l, String t, AttributeSet a) throws BadLocationException
        { if (t.matches("\\d+")) super.replace(fb, o, l, t, a); }
    }
    
    private void tampilkanStruk(int idNota, String namaCustomer, double total, double bayar) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Struk Pembayaran #" + idNota, true);
        dialog.setSize(360, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        StringBuilder sb = new StringBuilder();
        sb.append("======================================\n");
        sb.append(String.format("%24s\n", "TOKO BERKAH JAYA"));
        sb.append(String.format("%27s\n", "Jl. Raya Berkah No. 88"));
        sb.append(String.format("%25s\n", "Telp: 08123456789"));
        sb.append("======================================\n");
        sb.append(" Nota    : #").append(idNota).append("\n");
        sb.append(" Tanggal : ").append(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date())).append("\n");
        sb.append(" Customer: ").append(namaCustomer).append("\n");
        sb.append(" Kasir   : ").append(namaUserLogin).append("\n");
        sb.append("--------------------------------------\n");

        for (ItemKeranjang ik : listItemKeranjang) {
            sb.append(" ").append(ik.namaBarang).append("\n");
            String qtyHarga = "   " + ik.qty + " x " + (int)ik.hargaSatuan;
            String sub     = String.valueOf((int)ik.subTotal);
            int space = 37 - qtyHarga.length() - sub.length();
            sb.append(qtyHarga).append(" ".repeat(Math.max(1, space))).append(sub).append("\n");
        }

        sb.append("--------------------------------------\n");
        
        String txtTotal = String.valueOf((int)total);
        String txtBayar = String.valueOf((int)bayar);
        String txtKembali = String.valueOf((int)(bayar - total));
        
        if (totalPajak > 0) {
            String txtSub = String.valueOf((int)(total - totalPajak));
            String txtPpn = String.valueOf((int)totalPajak);
            sb.append(" Subtotal  : ").append(" ".repeat(Math.max(1, 25 - txtSub.length()))).append(txtSub).append("\n");
            sb.append(" PPN (11%) : ").append(" ".repeat(Math.max(1, 25 - txtPpn.length()))).append(txtPpn).append("\n");
            sb.append("--------------------------------------\n");
        }
        
        sb.append(" TOTAL     : ").append(" ".repeat(Math.max(1, 25 - txtTotal.length()))).append(txtTotal).append("\n");
        sb.append(" BAYAR     : ").append(" ".repeat(Math.max(1, 25 - txtBayar.length()))).append(txtBayar).append("\n");
        sb.append(" KEMBALIAN : ").append(" ".repeat(Math.max(1, 25 - txtKembali.length()))).append(txtKembali).append("\n");
        
        sb.append("======================================\n");
        sb.append(String.format("%27s\n", "TERIMA KASIH"));
        sb.append(String.format("%31s\n", "Tank Bawa Kayu, Tengkyu"));
        sb.append(String.format("%29s\n", "Barang yang sudah dibeli"));
        sb.append(String.format("%30s\n", "tidak dapat ditukar kembali"));
        sb.append("======================================\n");

        JTextArea txtStruk = new JTextArea(sb.toString());
        txtStruk.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtStruk.setEditable(false);
        txtStruk.setBackground(new Color(250, 250, 243)); 
        txtStruk.setBorder(new EmptyBorder(10, 15, 10, 15));

        JScrollPane scroll = new JScrollPane(txtStruk);
        dialog.add(scroll, BorderLayout.CENTER);

        // FIX: Inject HTML & Font Segoe UI Emoji agar icon printer di modal dialog struk keluar berwarna riil
        JButton btnPrint = new JButton("<html><font face='Segoe UI Emoji'>🖨️</font> Cetak Struk Ril</html>");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.setBackground(new Color(46, 204, 113));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrint.addActionListener(e -> {
            try {
                txtStruk.print(null, null, true, null, null, true);
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Printer tidak merespon: " + ex.getMessage());
            }
        });

        dialog.add(btnPrint, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    public void updateStatusPajakSistem() {
        boolean pakaiPpn = Settings.isPakaiPpn();
        if (lblPpnPajak != null && tfPpnPajak != null) {
            lblPpnPajak.setVisible(pakaiPpn);
            tfPpnPajak.setVisible(pakaiPpn);
        }
        
        hitungGrandTotal();
        updateTampilanTotal();
        hitungKembalian();
        
        this.revalidate();
        this.repaint();
    }
}