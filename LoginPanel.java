import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.sql.*;

public class LoginPanel extends JPanel {
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;
    private JPanel loginForm;
    private PoultryApp mainApp;

    public LoginPanel(PoultryApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        
        // Main container
        JPanel container = new JPanel(new GridBagLayout());
        container.setBackground(new Color(245, 247, 250));
        
        // Login form panel
        loginForm = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        loginForm.setLayout(new GridBagLayout());
        loginForm.setPreferredSize(new Dimension(400, 450));
        loginForm.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        // Title
        JLabel titleLabel = new JLabel("Welcome Back!");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(44, 62, 80));
        
        // Subtitle
        JLabel subtitleLabel = new JLabel("Sign in to continue");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(127, 140, 141));
        
        // Form fields
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(44, 62, 80));
        
        txtUser = new JTextField(20);
        styleTextField(txtUser);
        
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passLabel.setForeground(new Color(44, 62, 80));
        
        txtPass = new JPasswordField(20);
        stylePasswordField(txtPass);
        
        // Login button
        btnLogin = new JButton("Sign In");
        styleButton(btnLogin, new Color(52, 152, 219));
        btnLogin.addActionListener(e -> login());
        
        // Forgot password link
        JLabel forgotPassword = new JLabel("Forgot password?");
        forgotPassword.setForeground(new Color(52, 152, 219));
        forgotPassword.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JOptionPane.showMessageDialog(LoginPanel.this, 
                    "Please contact your administrator to reset your password.", 
                    "Forgot Password", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                forgotPassword.setText("<html><u>Forgot password?</u></html>");
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                forgotPassword.setText("Forgot password?");
            }
        });
        
        // Add components to form with proper spacing
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 5, 0);
        loginForm.add(titleLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginForm.add(subtitleLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        loginForm.add(userLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginForm.add(txtUser, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 5, 0);
        loginForm.add(passLabel, gbc);
        
        gbc.gridy++;
        gbc.insets = new Insets(0, 0, 10, 0);
        loginForm.add(txtPass, gbc);
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginForm.add(forgotPassword, gbc);
        
        gbc.gridy++;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 0, 0);
        loginForm.add(btnLogin, gbc);
        
        // Add login form to container
        container.add(loginForm);
        
        // Add container to main panel
        add(container, BorderLayout.CENTER);
        
        // Add keyboard enter key support
        txtPass.addActionListener(e -> login());
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 223, 230)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        field.setBackground(new Color(250, 250, 252));
        field.setPreferredSize(new Dimension(300, 45));
    }
    
    private void stylePasswordField(JPasswordField field) {
        styleTextField(field);
        field.setEchoChar('•');
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(300, 45));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(
                    Math.max(0, color.getRed() - 20),
                    Math.max(0, color.getGreen() - 20),
                    Math.max(0, color.getBlue() - 20)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, 
            "<html><div style='text-align: center;'>" + message + "</div></html>", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
    
    private void login() {
        String username = txtUser.getText().trim();
        String password = new String(txtPass.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }
        
        // Show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Signing in...");
        
        // Run database operation in background
        new SwingWorker<Boolean, Void>() {
            private int userId;
            private String role;
            private String errorMessage;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                String sql = "SELECT user_id, role FROM users WHERE username=? AND password=? LIMIT 1";
                try (Connection conn = DBConnection.connect();
                     PreparedStatement pst = conn.prepareStatement(sql)) {
                    pst.setString(1, username);
                    pst.setString(2, password); // NOTE: store hashed passwords in production
                    try (ResultSet rs = pst.executeQuery()) {
                        if (rs.next()) {
                            userId = rs.getInt("user_id");
                            role = rs.getString("role");
                            return true;
                        } else {
                            errorMessage = "Invalid username or password.";
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    if (ex instanceof SQLException) {
                        errorMessage = "Database connection error. Please try again later.";
                    } else {
                        errorMessage = "Login error: " + ex.getMessage();
                    }
                    ex.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    if (get()) {
                        // Login successful
                        mainApp.onLoginSuccess(userId, username, role);
                    } else {
                        showError(errorMessage);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("An unexpected error occurred.");
                } finally {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign In");
                }
            }
        }.execute();
    }
}
