import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.EmptyBorder;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {
    private int userId;
    private Runnable onAddRecordClick;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Dashboard");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(44, 62, 80));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Stats Panel (clean 3-card row, Alerts removed)
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        statsPanel.add(createStatCard("Eggs Today", "1,245", new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Birds Healthy", "98%", new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Feed Stock", "85%", new Color(230, 126, 34)));

        // Quick Actions Panel
        JPanel quickActions = new JPanel(new GridLayout(1, 3, 15, 15));
        quickActions.setOpaque(false);
        quickActions.setBorder(new EmptyBorder(20, 0, 0, 0));

        quickActions.add(createQuickAction("Add Record", "📝"));
        quickActions.add(createQuickAction("View Reports", "📊"));
        // Removed Manage Users and Settings quick action buttons

        // Add panels
        add(headerPanel, BorderLayout.NORTH);
        add(statsPanel, BorderLayout.CENTER);
        add(quickActions, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                g2d.dispose();
            }
        };

        card.setLayout(new BorderLayout(10, 10));
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(127, 140, 141));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JButton createQuickAction(String label, String emoji) {
        JButton button = new JButton("<html><center>" + emoji + "<br>" + label + "</center></html>") {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
                g2d.dispose();
            }

            @Override
            public boolean isContentAreaFilled() {
                return false;
            }
        };

        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(44, 62, 80));
        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(20, 10, 20, 10));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(Color.WHITE);

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 240, 240));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        // Actions for specific quick action buttons
        if ("View Reports".equals(label)) {
            button.addActionListener(e -> showReportsDialog());
        }
        if ("Add Record".equals(label)) {
            button.addActionListener(e -> {
                if (onAddRecordClick != null) {
                    onAddRecordClick.run();
                }
            });
        }

        return button;
    }

    private void showReportsDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Generate Farm Report", true);
        dialog.setSize(700, 500);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Generate Farm Report", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        // Controls: report type + date + buttons
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controls.add(new JLabel("Report Type:"));
        String[] types = {"Daily", "Weekly", "Monthly"};
        JComboBox<String> cmbType = new JComboBox<>(types);
        controls.add(cmbType);

        controls.add(new JLabel("Date (YYYY-MM-DD):"));
        JTextField txtDate = new JTextField(10);
        txtDate.setText(LocalDate.now().toString());
        controls.add(txtDate);

        JButton btnGenerate = new JButton("Generate");
        JButton btnExport = new JButton("Export...");
        controls.add(btnGenerate);
        controls.add(btnExport);

        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 13));

        content.add(title, BorderLayout.NORTH);
        content.add(controls, BorderLayout.CENTER);
        content.add(new JScrollPane(reportArea), BorderLayout.SOUTH);

        // Use BorderLayout weights
        ((BorderLayout) content.getLayout()).addLayoutComponent(title, BorderLayout.NORTH);
        ((BorderLayout) content.getLayout()).addLayoutComponent(controls, BorderLayout.CENTER);
        ((BorderLayout) content.getLayout()).addLayoutComponent(new JScrollPane(reportArea), BorderLayout.SOUTH);

        btnGenerate.addActionListener(e -> {
            String type = (String) cmbType.getSelectedItem();
            String dateStr = txtDate.getText().trim();
            if (dateStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter a date.");
                return;
            }
            try {
                LocalDate baseDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
                String report = generateFarmReport(type, baseDate);
                reportArea.setText(report);
                reportArea.setCaretPosition(0);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Error generating report: " + ex.getMessage());
            }
        });

        btnExport.addActionListener(e -> {
            if (reportArea.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Generate a report first.");
                return;
            }
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Export Report");
            if (chooser.showSaveDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = chooser.getSelectedFile();
                try (java.io.FileWriter fw = new java.io.FileWriter(file)) {
                    fw.write(reportArea.getText());
                    JOptionPane.showMessageDialog(dialog, "Report exported to " + file.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog, "Error exporting report: " + ex.getMessage());
                }
            }
        });

        dialog.setContentPane(content);
        dialog.setVisible(true);
    }

    private String generateFarmReport(String type, LocalDate baseDate) throws SQLException {
        LocalDate start;
        LocalDate end;

        switch (type) {
            case "Weekly":
                // Week: base date and previous 6 days
                end = baseDate;
                start = baseDate.minusDays(6);
                break;
            case "Monthly":
                start = baseDate.withDayOfMonth(1);
                end = baseDate.withDayOfMonth(baseDate.lengthOfMonth());
                break;
            default: // Daily
                start = baseDate;
                end = baseDate;
                break;
        }

        String startStr = start.toString();
        String endStr = end.toString();

        StringBuilder sb = new StringBuilder();
        sb.append("Farm Report - ").append(type).append("\n");
        sb.append("Period: ").append(startStr).append(" to ").append(endStr).append("\n\n");

        try (Connection conn = DBConnection.connect()) {
            // Egg production summary
            String eggsSql = "SELECT COALESCE(SUM(small_eggs),0) AS s, COALESCE(SUM(medium_eggs),0) AS m, " +
                    "COALESCE(SUM(large_eggs),0) AS l, COALESCE(SUM(broken_eggs),0) AS b " +
                    "FROM egg_production WHERE record_date BETWEEN ? AND ?";
            try (PreparedStatement pst = conn.prepareStatement(eggsSql)) {
                pst.setString(1, startStr);
                pst.setString(2, endStr);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int s = rs.getInt("s");
                        int m = rs.getInt("m");
                        int l = rs.getInt("l");
                        int b = rs.getInt("b");
                        int total = s + m + l;
                        sb.append("Egg Production\n");
                        sb.append(String.format("  Small:  %d\n", s));
                        sb.append(String.format("  Medium: %d\n", m));
                        sb.append(String.format("  Large:  %d\n", l));
                        sb.append(String.format("  Broken: %d\n", b));
                        sb.append(String.format("  TOTAL:  %d\n\n", total));
                    }
                }
            }

            // Bird health summary
            String healthSql = "SELECT COALESCE(SUM(total_birds),0) AS total, COALESCE(SUM(healthy_birds),0) AS healthy, " +
                    "COALESCE(SUM(sick_birds),0) AS sick FROM bird_health WHERE record_date BETWEEN ? AND ?";
            try (PreparedStatement pst = conn.prepareStatement(healthSql)) {
                pst.setString(1, startStr);
                pst.setString(2, endStr);
                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        int healthy = rs.getInt("healthy");
                        int sick = rs.getInt("sick");
                        sb.append("Bird Health\n");
                        sb.append(String.format("  Total birds (sum of records):   %d\n", total));
                        sb.append(String.format("  Healthy birds (sum):           %d\n", healthy));
                        sb.append(String.format("  Sick birds (sum):              %d\n", sick));
                        if (total > 0) {
                            double healthyPct = (healthy * 100.0) / total;
                            sb.append(String.format("  Healthy percentage (approx.):  %.2f%%\n", healthyPct));
                        }
                        sb.append("\n");
                    }
                }
            }

            // Feed usage summary
            String feedSql = "SELECT feed_type, COALESCE(SUM(quantity_used),0) AS used " +
                    "FROM feed_usage WHERE record_date BETWEEN ? AND ? GROUP BY feed_type";
            try (PreparedStatement pst = conn.prepareStatement(feedSql)) {
                pst.setString(1, startStr);
                pst.setString(2, endStr);
                try (ResultSet rs = pst.executeQuery()) {
                    sb.append("Feed Usage\n");
                    boolean any = false;
                    while (rs.next()) {
                        any = true;
                        String ft = rs.getString("feed_type");
                        double used = rs.getDouble("used");
                        sb.append(String.format("  %-20s %.2f\n", ft, used));
                    }
                    if (!any) {
                        sb.append("  No feed usage records in this period.\n");
                    }
                    sb.append("\n");
                }
            }
        }

        sb.append("End of report.\n");
        return sb.toString();
    }

    public void setCurrentUser(int userId) {
        this.userId = userId;
        // Reload dashboard data if needed
    }

    public void setOnAddRecordClick(Runnable onAddRecordClick) {
        this.onAddRecordClick = onAddRecordClick;
    }
}
