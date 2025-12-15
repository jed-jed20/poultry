import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class FeedUsagePanel extends JPanel {
    private JTextField txtDate, txtFeedType, txtQuantityUsed, txtRemaining;
    private JTextArea txtNotes;
    private JButton btnAdd, btnView, btnUpdate, btnDelete, btnExport;
    private JTable table;
    private DefaultTableModel model;
    private int currentUserId = -1;

    public FeedUsagePanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setPreferredSize(new Dimension(880, 560));

        add(new JLabel("Date (YYYY-MM-DD):")); txtDate = new JTextField(10); add(txtDate);
        add(new JLabel("Feed Type:")); txtFeedType = new JTextField(12); add(txtFeedType);
        add(new JLabel("Quantity Used:")); txtQuantityUsed = new JTextField(8); add(txtQuantityUsed);
        add(new JLabel("Remaining Stock:")); txtRemaining = new JTextField(8); add(txtRemaining);
        add(new JLabel("Notes:")); txtNotes = new JTextArea(3,50); add(new JScrollPane(txtNotes));

        btnAdd = new JButton("Add"); btnAdd.setForeground(Color.BLACK); add(btnAdd);
        btnView = new JButton("View"); btnView.setForeground(Color.BLACK); add(btnView);
        btnUpdate = new JButton("Update"); btnUpdate.setForeground(Color.BLACK); add(btnUpdate);
        btnDelete = new JButton("Delete"); btnDelete.setForeground(Color.BLACK); add(btnDelete);
        btnExport = new JButton("Export CSV"); btnExport.setForeground(Color.BLACK); add(btnExport);

        model = new DefaultTableModel(new String[]{"ID","Date","Feed Type","Qty Used","Remaining","Notes"}, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(860, 320));
        add(sp);

        btnAdd.addActionListener(e -> addRecord());
        btnView.addActionListener(e -> loadRecords());
        btnUpdate.addActionListener(e -> updateRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnExport.addActionListener(e -> exportCSV());

        table.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                int r = table.getSelectedRow();
                if (r>=0) {
                    txtDate.setText(model.getValueAt(r,1).toString());
                    txtFeedType.setText(model.getValueAt(r,2).toString());
                    txtQuantityUsed.setText(model.getValueAt(r,3).toString());
                    txtRemaining.setText(model.getValueAt(r,4).toString());
                    txtNotes.setText(model.getValueAt(r,5).toString());
                }
            }
        });
    }

    public void setCurrentUser(int userId) { this.currentUserId = userId; }

    private void addRecord() {
        if (txtDate.getText().isEmpty() || txtFeedType.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill required fields.");
            return;
        }

        double qtyUsed;
        try {
            qtyUsed = Double.parseDouble(defaultZeroDecimal(txtQuantityUsed.getText()));
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Quantity Used must be a number.");
            return;
        }

        Connection conn = null;
        try {
            conn = DBConnection.connect();
            conn.setAutoCommit(false);

            String feedType = txtFeedType.getText();

            // Get current total stock for this feed type from feed_inventory
            double currentStock = 0.0;
            String stockSql = "SELECT COALESCE(SUM(quantity_added - quantity_removed),0) AS stock FROM feed_inventory WHERE feed_type = ?";
            try (PreparedStatement pstStock = conn.prepareStatement(stockSql)) {
                pstStock.setString(1, feedType);
                try (ResultSet rs = pstStock.executeQuery()) {
                    if (rs.next()) {
                        currentStock = rs.getDouble("stock");
                    }
                }
            }

            double newStock = currentStock - qtyUsed;
            if (newStock < 0) newStock = 0; // prevent negative stock

            // Insert usage record with computed remaining_stock
            String sql = "INSERT INTO feed_usage (record_date, feed_type, quantity_used, remaining_stock, notes, recorded_by) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, txtDate.getText());
                pst.setString(2, feedType);
                pst.setDouble(3, qtyUsed);
                pst.setDouble(4, newStock);
                pst.setString(5, txtNotes.getText());
                pst.setInt(6, (currentUserId>0)?currentUserId:0);
                pst.executeUpdate();
            }

            // Also write a corresponding inventory transaction to reflect the deduction
            String invSql = "INSERT INTO feed_inventory (transaction_date, feed_type, quantity_added, quantity_removed, notes) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement pstInv = conn.prepareStatement(invSql)) {
                pstInv.setString(1, txtDate.getText());
                pstInv.setString(2, feedType);
                pstInv.setDouble(3, 0.0); // no addition
                pstInv.setDouble(4, qtyUsed); // deduction
                pstInv.setString(5, "Auto deduction from feed usage");
                pstInv.executeUpdate();
            }

            conn.commit();

            txtRemaining.setText(String.valueOf(newStock));

            JOptionPane.showMessageDialog(this, "Record added and inventory updated!");
            loadRecords();
        } catch (Exception ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding record. Check console for details.");
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadRecords() {
        try (Connection conn = DBConnection.connect()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM feed_usage ORDER BY record_date DESC");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("feed_id"),
                    rs.getString("record_date"),
                    rs.getString("feed_type"),
                    rs.getDouble("quantity_used"),
                    rs.getDouble("remaining_stock"),
                    rs.getString("notes")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateRecord() {
        int r = table.getSelectedRow();
        if (r==-1) { JOptionPane.showMessageDialog(this, "Select row."); return; }
        int id = Integer.parseInt(model.getValueAt(r,0).toString());
        try (Connection conn = DBConnection.connect()) {
            String sql = "UPDATE feed_usage SET record_date=?, feed_type=?, quantity_used=?, remaining_stock=?, notes=?, recorded_by=? WHERE feed_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setString(2, txtFeedType.getText());
            pst.setDouble(3, Double.parseDouble(defaultZeroDecimal(txtQuantityUsed.getText())));
            pst.setDouble(4, Double.parseDouble(defaultZeroDecimal(txtRemaining.getText())));
            pst.setString(5, txtNotes.getText());
            pst.setInt(6, (currentUserId>0)?currentUserId:0);
            pst.setInt(7, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Updated.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteRecord() {
        int r = table.getSelectedRow();
        if (r==-1) { JOptionPane.showMessageDialog(this, "Select row."); return; }
        int id = Integer.parseInt(model.getValueAt(r,0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm!=JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnection.connect()) {
            PreparedStatement pst = conn.prepareStatement("DELETE FROM feed_usage WHERE feed_id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void exportCSV() {
        try (java.io.FileWriter csv = new java.io.FileWriter("feed_usage_export.csv")) {
            for (int i=0;i<model.getRowCount();i++) {
                csv.append(model.getValueAt(i,0).toString()).append(",");
                csv.append(model.getValueAt(i,1).toString()).append(",");
                csv.append(model.getValueAt(i,2).toString()).append(",");
                csv.append(model.getValueAt(i,3).toString()).append(",");
                csv.append(model.getValueAt(i,4).toString()).append(",");
                csv.append("\"").append(model.getValueAt(i,5).toString().replace("\"","\"\"")).append("\"\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to feed_usage_export.csv");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String defaultZeroDecimal(String s) { return (s==null || s.trim().isEmpty()) ? "0" : s.trim(); }
}
    