import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;
import java.sql.*;

public class BirdHealthPanel extends JPanel {
    private JTextField txtDate, txtTotal, txtHealthy, txtSick;
    private JTextArea txtNotes;
    private JButton btnAdd, btnView, btnUpdate, btnDelete, btnExport;
    private JTable table;
    private DefaultTableModel model;
    private int currentUserId = -1;

    public BirdHealthPanel() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        setPreferredSize(new Dimension(880, 560));

        add(new JLabel("Date (YYYY-MM-DD):")); txtDate = new JTextField(10); add(txtDate);
        add(new JLabel("Total Birds:")); txtTotal = new JTextField(6); add(txtTotal);
        add(new JLabel("Healthy:")); txtHealthy = new JTextField(6); add(txtHealthy);
        add(new JLabel("Sick:")); txtSick = new JTextField(6); add(txtSick);

        add(new JLabel("Notes:"));
        txtNotes = new JTextArea(3,50);
        add(new JScrollPane(txtNotes));

        btnAdd = new JButton("Add"); btnAdd.setForeground(Color.BLACK); add(btnAdd);
        btnView = new JButton("View"); btnView.setForeground(Color.BLACK); add(btnView);
        btnUpdate = new JButton("Update"); btnUpdate.setForeground(Color.BLACK); add(btnUpdate);
        btnDelete = new JButton("Delete"); btnDelete.setForeground(Color.BLACK); add(btnDelete);
        btnExport = new JButton("Export CSV"); btnExport.setForeground(Color.BLACK); add(btnExport);

        model = new DefaultTableModel(new String[]{"ID","Date","Total","Healthy","Sick","Notes"}, 0);
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
                    txtTotal.setText(model.getValueAt(r,2).toString());
                    txtHealthy.setText(model.getValueAt(r,3).toString());
                    txtSick.setText(model.getValueAt(r,4).toString());
                    txtNotes.setText(model.getValueAt(r,5).toString());
                }
            }
        });

        setControlsEnabled(false);
    }

    public void setCurrentUser(int userId) { this.currentUserId = userId; }

    public void setControlsEnabled(boolean enabled) {
        txtDate.setEnabled(enabled);
        txtTotal.setEnabled(enabled);
        txtHealthy.setEnabled(enabled);
        txtSick.setEnabled(enabled);
        txtNotes.setEnabled(enabled);
        btnAdd.setEnabled(enabled);
        btnView.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        table.setEnabled(enabled);
    }

    private void addRecord() {
        if (txtDate.getText().isEmpty()) { JOptionPane.showMessageDialog(this, "Fill date."); return; }
        try (Connection conn = DBConnection.connect()) {
            String sql = "INSERT INTO bird_health (record_date, total_birds, healthy_birds, sick_birds, notes, recorded_by) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setInt(2, Integer.parseInt(defaultZero(txtTotal.getText())));
            pst.setInt(3, Integer.parseInt(defaultZero(txtHealthy.getText())));
            pst.setInt(4, Integer.parseInt(defaultZero(txtSick.getText())));
            pst.setString(5, txtNotes.getText());
            pst.setInt(6, (currentUserId>0)?currentUserId:0);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Record added!");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadRecords() {
        try (Connection conn = DBConnection.connect()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM bird_health ORDER BY record_date DESC");
            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("health_id"),
                    rs.getString("record_date"),
                    rs.getInt("total_birds"),
                    rs.getInt("healthy_birds"),
                    rs.getInt("sick_birds"),
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
            String sql = "UPDATE bird_health SET record_date=?, total_birds=?, healthy_birds=?, sick_birds=?, notes=?, recorded_by=? WHERE health_id=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, txtDate.getText());
            pst.setInt(2, Integer.parseInt(defaultZero(txtTotal.getText())));
            pst.setInt(3, Integer.parseInt(defaultZero(txtHealthy.getText())));
            pst.setInt(4, Integer.parseInt(defaultZero(txtSick.getText())));
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
            PreparedStatement pst = conn.prepareStatement("DELETE FROM bird_health WHERE health_id=?");
            pst.setInt(1, id);
            pst.executeUpdate();
            JOptionPane.showMessageDialog(this, "Deleted.");
            loadRecords();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void exportCSV() {
        try (java.io.FileWriter csv = new java.io.FileWriter("bird_health_export.csv")) {
            for (int i=0;i<model.getRowCount();i++) {
                csv.append(model.getValueAt(i,0).toString()).append(",");
                csv.append(model.getValueAt(i,1).toString()).append(",");
                csv.append(model.getValueAt(i,2).toString()).append(",");
                csv.append(model.getValueAt(i,3).toString()).append(",");
                csv.append(model.getValueAt(i,4).toString()).append(",");
                csv.append("\"").append(model.getValueAt(i,5).toString().replace("\"","\"\"")).append("\"\n");
            }
            JOptionPane.showMessageDialog(this, "Exported to bird_health_export.csv");
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private String defaultZero(String s) { return (s==null || s.trim().isEmpty()) ? "0" : s.trim(); }
}
