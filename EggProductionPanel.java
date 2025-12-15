import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class EggProductionPanel extends JPanel {
    private JTextField txtDate, txtSmall, txtMedium, txtLarge, txtBroken;
    private JButton btnAdd, btnView, btnUpdate, btnDelete, btnExport;
    private JTable table;
    private DefaultTableModel model;
    private int currentUserId = -1;

    public EggProductionPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setPreferredSize(new Dimension(880, 560));

        add(new JLabel("Date (YYYY-MM-DD):"));
        txtDate = new JTextField(10); add(txtDate);

        add(new JLabel("Small:"));
        txtSmall = new JTextField(5); add(txtSmall);

        add(new JLabel("Medium:"));
        txtMedium = new JTextField(5); add(txtMedium);

        add(new JLabel("Large:"));
        txtLarge = new JTextField(5); add(txtLarge);

        add(new JLabel("Broken:"));
        txtBroken = new JTextField(5); add(txtBroken);

        btnAdd = new JButton("Add"); btnAdd.setForeground(Color.BLACK); add(btnAdd);
        btnView = new JButton("View"); btnView.setForeground(Color.BLACK); add(btnView);
        btnUpdate = new JButton("Update"); btnUpdate.setForeground(Color.BLACK); add(btnUpdate);
        btnDelete = new JButton("Delete"); btnDelete.setForeground(Color.BLACK); add(btnDelete);
        btnExport = new JButton("Export CSV"); btnExport.setForeground(Color.BLACK); add(btnExport);

        model = new DefaultTableModel(new String[]{"ID","Date","Small","Medium","Large","Broken"}, 0);
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);
        sp.setPreferredSize(new Dimension(860, 380));
        add(sp);

        btnAdd.addActionListener(e -> addRecord());
        btnView.addActionListener(e -> loadRecords());
        btnUpdate.addActionListener(e -> updateRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnExport.addActionListener(e -> exportCSV());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    txtDate.setText(model.getValueAt(r,1).toString());
                    txtSmall.setText(model.getValueAt(r,2).toString());
                    txtMedium.setText(model.getValueAt(r,3).toString());
                    txtLarge.setText(model.getValueAt(r,4).toString());
                    txtBroken.setText(model.getValueAt(r,5).toString());
                }
            }
        });
    }

    public void setCurrentUser(int userId) { this.currentUserId = userId; }

    private void addRecord() {
        if (txtDate.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill date."); return; }
        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO egg_production (record_date, small_eggs, medium_eggs, large_eggs, broken_eggs, recorded_by) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setInt(2, Integer.parseInt(defaultZero(txtSmall.getText())));
            pst.setInt(3, Integer.parseInt(defaultZero(txtMedium.getText())));
            pst.setInt(4, Integer.parseInt(defaultZero(txtLarge.getText())));
            pst.setInt(5, Integer.parseInt(defaultZero(txtBroken.getText())));
            pst.setInt(6, (currentUserId > 0) ? currentUserId : 0);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record added!");
            loadRecords();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: "+ex.getMessage());
        }
    }

    private void loadRecords() {
        try (Connection conn = DBConnection.connect()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM egg_production ORDER BY record_date DESC");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("production_id"),
                    rs.getString("record_date"),
                    rs.getInt("small_eggs"),
                    rs.getInt("medium_eggs"),
                    rs.getInt("large_eggs"),
                    rs.getInt("broken_eggs")
                });
            }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateRecord() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select row to update."); return; }
        int id = Integer.parseInt(model.getValueAt(r,0).toString());
        try (Connection conn = DBConnection.connect()) {
            String sql = "UPDATE egg_production SET record_date=?, small_eggs=?, medium_eggs=?, large_eggs=?, broken_eggs=?, recorded_by=? WHERE production_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setInt(2, Integer.parseInt(defaultZero(txtSmall.getText())));
            pst.setInt(3, Integer.parseInt(defaultZero(txtMedium.getText())));
            pst.setInt(4, Integer.parseInt(defaultZero(txtLarge.getText())));
            pst.setInt(5, Integer.parseInt(defaultZero(txtBroken.getText())));
            pst.setInt(6, (currentUserId > 0) ? currentUserId : 0);
            pst.setInt(7, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Updated.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void deleteRecord() {
        int r = table.getSelectedRow();
        if (r == -1) { JOptionPane.showMessageDialog(this, "Select row to delete."); return; }
        int id = Integer.parseInt(model.getValueAt(r,0).toString());
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected record?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try (Connection conn = DBConnection.connect()) {
            String sql = "DELETE FROM egg_production WHERE production_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void exportCSV() {
        try (java.io.FileWriter csv = new java.io.FileWriter("egg_production_export.csv")) {
            for (int i=0;i<model.getRowCount();i++) {
                csv.append(model.getValueAt(i,0).toString()).append(",");
                csv.append(model.getValueAt(i,1).toString()).append(",");
                csv.append(model.getValueAt(i,2).toString()).append(",");
                csv.append(model.getValueAt(i,3).toString()).append(",");
                csv.append(model.getValueAt(i,4).toString()).append(",");
                csv.append(model.getValueAt(i,5).toString()).append("\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to egg_production_export.csv");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String defaultZero(String s) { return (s==null || s.trim().isEmpty()) ? "0" : s.trim(); }
}
