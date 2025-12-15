import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class FeedInventoryPanel extends JPanel {
    private JTextField txtDate, txtFeedType, txtAdded, txtRemoved;
    private JTextArea txtNotes;
    private JButton btnAdd, btnView, btnUpdate, btnDelete, btnExport;
    private JTable table;
    private DefaultTableModel model;
    private int currentUserId = -1;

    public FeedInventoryPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setPreferredSize(new Dimension(880, 560));

        add(new JLabel("Date (YYYY-MM-DD):")); txtDate = new JTextField(10); add(txtDate);
        add(new JLabel("Feed Type:")); txtFeedType = new JTextField(12); add(txtFeedType);
        add(new JLabel("Qty Added:")); txtAdded = new JTextField(8); add(txtAdded);
        add(new JLabel("Qty Removed:")); txtRemoved = new JTextField(8); add(txtRemoved);
        add(new JLabel("Notes:")); txtNotes = new JTextArea(3,50); add(new JScrollPane(txtNotes));

        btnAdd = new JButton("Add"); btnAdd.setForeground(Color.BLACK); add(btnAdd);
        btnView = new JButton("View"); btnView.setForeground(Color.BLACK); add(btnView);
        btnUpdate = new JButton("Update"); btnUpdate.setForeground(Color.BLACK); add(btnUpdate);
        btnDelete = new JButton("Delete"); btnDelete.setForeground(Color.BLACK); add(btnDelete);
        btnExport = new JButton("Export CSV"); btnExport.setForeground(Color.BLACK); add(btnExport);

        model = new DefaultTableModel(new String[]{"ID","Date","Feed Type","Added","Removed","Notes"}, 0);
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
                    txtAdded.setText(model.getValueAt(r,3).toString());
                    txtRemoved.setText(model.getValueAt(r,4).toString());
                    txtNotes.setText(model.getValueAt(r,5).toString());
                }
            }
        });
    }

    public void setCurrentUser(int userId) { this.currentUserId = userId; }

    private void addRecord() {
        if (txtDate.getText().isEmpty() || txtFeedType.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill required fields."); return; }
        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO feed_inventory (transaction_date, feed_type, quantity_added, quantity_removed, notes) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setString(2, txtFeedType.getText());
            pst.setDouble(3, Double.parseDouble(defaultZero(txtAdded.getText())));
            pst.setDouble(4, Double.parseDouble(defaultZero(txtRemoved.getText())));
            pst.setString(5, txtNotes.getText());
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record added!");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadRecords() {
        try (Connection conn = DBConnection.connect()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM feed_inventory ORDER BY transaction_date DESC");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("inventory_id"),
                    rs.getString("transaction_date"),
                    rs.getString("feed_type"),
                    rs.getDouble("quantity_added"),
                    rs.getDouble("quantity_removed"),
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
            String sql = "UPDATE feed_inventory SET transaction_date=?, feed_type=?, quantity_added=?, quantity_removed=?, notes=? WHERE inventory_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setString(2, txtFeedType.getText());
            pst.setDouble(3, Double.parseDouble(defaultZero(txtAdded.getText())));
            pst.setDouble(4, Double.parseDouble(defaultZero(txtRemoved.getText())));
            pst.setString(5, txtNotes.getText());
            pst.setInt(6, id);
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
            PreparedStatement pst = conn.prepareStatement("DELETE FROM feed_inventory WHERE inventory_id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void exportCSV() {
        try (java.io.FileWriter csv = new java.io.FileWriter("feed_inventory_export.csv")) {
            for (int i=0;i<model.getRowCount();i++) {
                csv.append(model.getValueAt(i,0).toString()).append(",");
                csv.append(model.getValueAt(i,1).toString()).append(",");
                csv.append(model.getValueAt(i,2).toString()).append(",");
                csv.append(model.getValueAt(i,3).toString()).append(",");
                csv.append(model.getValueAt(i,4).toString()).append(",");
                csv.append("\"").append(model.getValueAt(i,5).toString().replace("\"","\"\"")).append("\"\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to feed_inventory_export.csv");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String defaultZero(String s) { return (s==null || s.trim().isEmpty()) ? "0" : s.trim(); }
}
