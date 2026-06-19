package berkahjaya.ui;

import berkahjaya.dao.PenjualanDAO;
import berkahjaya.model.Penjualan;
import berkahjaya.model.DetailPenjualan;

import com.toedter.calendar.JDateChooser; 

import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LaporanPanel extends JPanel {

    private final PenjualanDAO penjualanDAO = new PenjualanDAO();
    private final NumberFormat rupiahFmt    = NumberFormat.getCurrencyInstance(new Locale("id","ID"));
    private final SimpleDateFormat sdf      = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private JTable            table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> rowSorter;
    private JLabel            lblTotal;
    
    private JTextField        tfCari;
    private JDateChooser      dcTglMulai, dcTglSampai; 

    public LaporanPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 25, 20, 25));
        initComponents();
        loadData(); 
    }

    private void initComponents() {
        JPanel topContainer = new JPanel(new GridLayout(2, 1, 0, 15));
        topContainer.setOpaque(false);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel title = new JLabel("LAPORAN RIWAYAT PENJUALAN");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(52, 152, 219));
        
        JButton btnExport = new JButton("EKSPOR LAPORAM");
        btnExport.setBackground(new Color(41, 128, 185));
        btnExport.setForeground(Color.WHITE);
        btnExport.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToCSV());
        
        titlePanel.add(title, BorderLayout.WEST);
        titlePanel.add(btnExport, BorderLayout.EAST);

        // --- 2. Filter Toolbar (DENGAN KALENDER) ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filterPanel.setOpaque(false);

        dcTglMulai = new JDateChooser();
        dcTglMulai.setDateFormatString("yyyy-MM-dd");
        dcTglMulai.setPreferredSize(new Dimension(140, 30));

        dcTglSampai = new JDateChooser();
        dcTglSampai.setDateFormatString("yyyy-MM-dd");
        dcTglSampai.setPreferredSize(new Dimension(140, 30));

        JButton btnFilter = new JButton("CARI DATA");
        btnFilter.addActionListener(e -> prosesFilterTanggal());

        tfCari = new JTextField(18);
        tfCari.putClientProperty("JTextField.placeholderText", "Cari Nama/Kasir...");
        tfCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + tfCari.getText()));
            }
        });

        filterBarLabel(filterPanel, "MULAI:");
        filterPanel.add(dcTglMulai);
        filterBarLabel(filterPanel, "SAMPAI:");
        filterPanel.add(dcTglSampai);
        filterPanel.add(btnFilter);
        filterPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        filterBarLabel(filterPanel, "CARI PINTAR:");
        filterPanel.add(tfCari);

        topContainer.add(titlePanel);
        topContainer.add(filterPanel);
        add(topContainer, BorderLayout.NORTH);

        // --- 3. Table Section ---
        String[] cols = {"ID TRANSAKSI", "WAKTU", "CUSTOMER", "TOTAL BAYAR", "KASIR"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        rowSorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(rowSorter);
        table.setRowHeight(38);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                
                if (col == 0) setHorizontalAlignment(CENTER);
                else if (col == 3) setHorizontalAlignment(RIGHT); 
                else setHorizontalAlignment(LEFT);
                
                if (col == 3 && !sel) setForeground(new Color(46, 204, 113));
                else setForeground(UIManager.getColor("Table.foreground"));
                
                setBorder(new EmptyBorder(0, 12, 0, 12));
                return this;
            }
        });

        // Event Double Click (Pop-up Struk Thermal Modern)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() != -1) {
                    int modelRow = table.convertRowIndexToModel(table.getSelectedRow());
                    
                    int idJual = Integer.parseInt(tableModel.getValueAt(modelRow, 0).toString());
                    
                    Object valCustomer = tableModel.getValueAt(modelRow, 2);
                    String customer = (valCustomer != null) ? valCustomer.toString() : "Pelanggan Umum";
                    
                    Object valWaktu = tableModel.getValueAt(modelRow, 1);
                    String waktu = (valWaktu != null) ? valWaktu.toString() : "-";
                    
                    Object valKasir = tableModel.getValueAt(modelRow, 4);
                    String kasir = (valKasir != null) ? valKasir.toString() : "-";
                    
                    tampilkanPopUpRincian(idJual, customer, waktu, kasir);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new LineBorder(new Color(50, 55, 60), 1));
        add(scroll, BorderLayout.CENTER);

        // --- 4. Footer ---
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(new Color(30, 35, 40));
        footerPanel.setBorder(new CompoundBorder(new LineBorder(new Color(50, 55, 60), 1), new EmptyBorder(15, 25, 15, 25)));

        lblTotal = new JLabel("TOTAL PENDAPATAN: Rp 0");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTotal.setForeground(new Color(46, 204, 113));
        footerPanel.add(lblTotal, BorderLayout.EAST);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void filterBarLabel(JPanel p, String txt) {
        JLabel l = new JLabel(txt);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Color.GRAY);
        p.add(l);
    }

    private void prosesFilterTanggal() {
        if (dcTglMulai.getDate() != null && dcTglSampai.getDate() != null) {
            if (dcTglMulai.getDate().after(dcTglSampai.getDate())) {
                JOptionPane.showMessageDialog(this, 
                    "Tanggal tidak valid! Tanggal MULAI tidak boleh melewati tanggal SAMPAI.", 
                    "Filter Gagal", 
                    JOptionPane.WARNING_MESSAGE);
                return; 
            }
        }

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd");
        String mulai = (dcTglMulai.getDate() != null) ? dbFormat.format(dcTglMulai.getDate()) : null;
        String sampai = (dcTglSampai.getDate() != null) ? dbFormat.format(dcTglSampai.getDate()) : null;
        
        loadData(mulai, sampai);
    }

    public void loadData() {
        loadData(null, null);
    }

    public void loadData(String tglMulai, String tglSampai) {
        tableModel.setRowCount(0);
        List<Penjualan> list = penjualanDAO.getRiwayatFiltered(tglMulai, tglSampai);
        double grandTotal = 0;
        
        for (int i = 0; i < list.size(); i++) {
            Penjualan p = list.get(i);
            grandTotal += p.getTotalBayar();
            
            tableModel.addRow(new Object[]{
                p.getIdJual(), 
                sdf.format(p.getTglTransaksi()), 
                p.getNamaCustomer(),
                rupiahFmt.format(p.getTotalBayar()), 
                p.getNamaUser() 
            });
        }
        lblTotal.setText("TOTAL PENDAPATAN: " + rupiahFmt.format(grandTotal) + " | " + list.size() + " TRANSAKSI");
    }

    // --- FITUR REVISI: POP-UP BERBENTUK STRUK KASIR THERMAL ---
    private void tampilkanPopUpRincian(int idJual, String customer, String waktu, String kasir) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Salinan Struk #" + idJual, true);
        dialog.setSize(360, 550);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        StringBuilder sb = new StringBuilder();
        sb.append("======================================\n");
        sb.append(String.format("%24s\n", "TOKO BERKAH JAYA"));
        sb.append(String.format("%27s\n", "Jl. Raya Berkah No. 88"));
        sb.append(String.format("%25s\n", "Telp: 08123456789"));
        sb.append("======================================\n");
        sb.append(" Nota    : #").append(idJual).append("\n");
        sb.append(" Tanggal : ").append(waktu).append("\n");
        sb.append(" Customer: ").append(customer).append("\n");
        sb.append(" Kasir   : ").append(kasir).append("\n");
        sb.append("--------------------------------------\n");

        List<DetailPenjualan> listDetail = penjualanDAO.getDetailByNota(idJual);
        double kalkulasiTotal = 0;

        for (DetailPenjualan dp : listDetail) {
            sb.append(" ").append(dp.getNamaBarang()).append("\n");
            int qty = dp.getJumlahBeli();
            double hargaSatuan = dp.getSubTotal() / qty;
            
            String qtyHarga = "   " + qty + " x " + (int)hargaSatuan;
            String sub     = String.valueOf((int)dp.getSubTotal());
            
            int space = 37 - qtyHarga.length() - sub.length();
            sb.append(qtyHarga).append(" ".repeat(Math.max(1, space))).append(sub).append("\n");
            
            kalkulasiTotal += dp.getSubTotal();
        }

        sb.append("--------------------------------------\n");
        String txtTotal = String.valueOf((int)kalkulasiTotal);
        
        sb.append(" TOTAL     : ").append(" ".repeat(Math.max(1, 25 - txtTotal.length()))).append(txtTotal).append("\n");
        sb.append(" BAYAR     : ").append(" ".repeat(Math.max(1, 25 - txtTotal.length()))).append(txtTotal).append("\n");
        sb.append(" KEMBALIAN : ").append(" ".repeat(Math.max(1, 24))).append("0\n");
        
        sb.append("======================================\n");
        sb.append(String.format("%31s\n", "[ STRUK SALINAN / REPRINT ]"));
        sb.append(String.format("%27s\n", "TERIMA KASIH"));
        sb.append(String.format("%31s\n", "MATUR NUWUN / THANK YOU"));
        sb.append("======================================\n");

        JTextArea txtStruk = new JTextArea(sb.toString());
        txtStruk.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtStruk.setEditable(false);
        txtStruk.setBackground(new Color(250, 250, 243)); 
        txtStruk.setBorder(new EmptyBorder(10, 15, 10, 15));

        JScrollPane scroll = new JScrollPane(txtStruk);
        dialog.add(scroll, BorderLayout.CENTER);

        JButton btnPrint = new JButton("🖨️ Cetak Ulang Struk");
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnPrint.setBackground(new Color(41, 128, 185)); 
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

    private void exportToCSV() {
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Tidak ada data untuk diekspor!");
            return;
        }

        try {
            java.io.File folderLaporan = new java.io.File("laporan");
            if (!folderLaporan.exists()) {
                folderLaporan.mkdirs(); // Otomatis bikin folder kalau belum ada
            }

            String tanggalHariIni = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String namaFile = "Laporan_Penjualan" + tanggalHariIni + ".csv";
            java.io.File fileToSave = new java.io.File(folderLaporan, namaFile);

            try (PrintWriter pw = new PrintWriter(new java.io.FileWriter(fileToSave))) {
                pw.println("ID TRANSAKSI;WAKTU;CUSTOMER;KASIR;NAMA PRODUK;QTY;SUB TOTAL;TOTAL NOTA");

                for (int i = 0; i < table.getRowCount(); i++) {
                    String idJualStr = table.getValueAt(i, 0).toString();
                    int idJual       = Integer.parseInt(idJualStr);
                    String waktu     = table.getValueAt(i, 1).toString();
                    String customer  = table.getValueAt(i, 2).toString().replace(";", " ");
                    String totalNota = table.getValueAt(i, 3).toString().replace(";", " ");
                    String kasir     = table.getValueAt(i, 4).toString().replace(";", " ");

                    List<DetailPenjualan> listDetail = penjualanDAO.getDetailByNota(idJual);
                    
                    for (DetailPenjualan dp : listDetail) {
                        String namaBarang = dp.getNamaBarang().replace(";", " ");
                        int qty           = dp.getJumlahBeli();
                        String subTotal   = rupiahFmt.format(dp.getSubTotal());

                        pw.println(idJual + ";" + waktu + ";" + customer + ";" + kasir + ";" + 
                                   namaBarang + ";" + qty + ";" + subTotal + ";" + totalNota);
                    }
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Ekspor Berhasil!\nFile disimpan di: " + fileToSave.getAbsolutePath(),
                    "Ekspor Sukses", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(folderLaporan);
                }
                
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Gagal ekspor: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}