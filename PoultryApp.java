import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class  PoultryApp {

    private JFrame frame;
    private JTabbedPane tabs;
    private JPanel mainPanel;

    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private EggProductionPanel eggPanel;
    private BirdHealthPanel healthPanel;
    private FeedUsagePanel feedUsagePanel;
    private FeedInventoryPanel inventoryPanel;

    private JButton btnLogout;
    private JButton btnBackDashboard;
    private JLabel userLabel;

    public PoultryApp() {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // Custom UI settings
            UIManager.put("TabbedPane.selected", new Color(52, 152, 219));
            UIManager.put("TabbedPane.background", new Color(245, 247, 250));
            UIManager.put("TabbedPane.foreground", new Color(44, 62, 80));
            UIManager.put("TabbedPane.borderHightlightColor", new Color(52, 152, 219));
            UIManager.put("TabbedPane.focus", new Color(52, 152, 219));
            UIManager.put("Button.foreground", new Color(255, 255, 255));
            UIManager.put("Button.background", new Color(52, 152, 219));
            UIManager.put("Button.focus", new Color(41, 128, 185));
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Poultry Farm Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1024, 768));
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(245, 247, 250));

        // Create main panel with card layout
        mainPanel = new JPanel(new CardLayout());
        mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Create top bar
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(255, 255, 255));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 236, 240)));
        topBar.setPreferredSize(new Dimension(frame.getWidth(), 60));

        // App title
        JLabel titleLabel = new JLabel("Poultry Farm Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(new EmptyBorder(0, 20, 0, 0));
        topBar.add(titleLabel, BorderLayout.WEST);

        // User info and logout button
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        userPanel.setOpaque(false);
        
        userLabel = new JLabel("");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userLabel.setForeground(new Color(52, 73, 94));
        
        btnLogout = new JButton("Logout");
        btnLogout.setEnabled(false);
        btnLogout.setBackground(new Color(231, 76, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.setBorderPainted(false);
        btnLogout.setOpaque(true);
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBackDashboard = new JButton("Back to Dashboard");
        btnBackDashboard.setEnabled(false);
        btnBackDashboard.setBackground(new Color(52, 152, 219));
        btnBackDashboard.setForeground(Color.WHITE);
        btnBackDashboard.setFocusPainted(false);
        btnBackDashboard.setBorderPainted(false);
        btnBackDashboard.setOpaque(true);
        btnBackDashboard.setCursor(new Cursor(Cursor.HAND_CURSOR));

        userPanel.add(userLabel);
        userPanel.add(Box.createHorizontalStrut(10));
        userPanel.add(btnBackDashboard);
        userPanel.add(btnLogout);
        userPanel.setBorder(new EmptyBorder(0, 0, 0, 20));
        
        topBar.add(userPanel, BorderLayout.EAST);
        
        frame.add(topBar, BorderLayout.NORTH);

        // Create panels
        loginPanel = new LoginPanel(this);
        dashboardPanel = new DashboardPanel();
        eggPanel = new EggProductionPanel();
        healthPanel = new BirdHealthPanel();
        feedUsagePanel = new FeedUsagePanel();
        inventoryPanel = new FeedInventoryPanel();

        // Create tabbed pane with custom UI
        tabs = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setBackground(new Color(245, 247, 250));
        tabs.setForeground(new Color(44, 62, 80));
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabs.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Add tabs (no icons to avoid NullPointerException when disabled)
        // Dashboard is now a separate screen, so tabs only contain data modules
        tabs.addTab("Egg Production", null, eggPanel, "Egg Production");
        tabs.addTab("Bird Health", null, healthPanel, "Bird Health");
        tabs.addTab("Feed Usage", null, feedUsagePanel, "Feed Usage");
        tabs.addTab("Feed Inventory", null, inventoryPanel, "Feed Inventory");
        
        // Disable all tabs initially
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setEnabledAt(i, false);
        }
        
        // Add cards to main panel: login, dashboard, and module tabs
        mainPanel.add(loginPanel, "login");
        mainPanel.add(dashboardPanel, "dashboard");
        mainPanel.add(tabs, "tabs");

        // When Add Record is clicked on the dashboard, go to modules (Egg Production tab)
        dashboardPanel.setOnAddRecordClick(() -> {
            CardLayout clInner = (CardLayout) mainPanel.getLayout();
            clInner.show(mainPanel, "tabs");
            // 0 = Egg Production, 1 = Bird Health, 2 = Feed Usage, 3 = Feed Inventory
            tabs.setSelectedIndex(0);
        });
        
        // Show login panel by default
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "login");
        
        frame.add(mainPanel, BorderLayout.CENTER);

        // LOGOUT ACTION
        btnLogout.addActionListener(e -> logout());

        // Back to Dashboard action
        btnBackDashboard.addActionListener(e -> {
            CardLayout clInner = (CardLayout) mainPanel.getLayout();
            clInner.show(mainPanel, "dashboard");
        });

        frame.setVisible(true);
    }

    // After successful login
    public void onLoginSuccess(int userId, String username, String role) {
        // Update UI
        userLabel.setText(username + " (" + role + ")");
        btnLogout.setEnabled(true);
        btnBackDashboard.setEnabled(true);
        
        // Set current user for all panels
        dashboardPanel.setCurrentUser(userId);
        eggPanel.setCurrentUser(userId);
        healthPanel.setCurrentUser(userId);
        feedUsagePanel.setCurrentUser(userId);
        inventoryPanel.setCurrentUser(userId);
        healthPanel.setControlsEnabled(true);
        
        // Enable all tabs
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setEnabledAt(i, true);
        }
        
        // Show dashboard after login
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "dashboard");
        
        // Show welcome message with custom dialog
        JDialog welcomeDialog = new JDialog(frame, "Welcome", true);
        welcomeDialog.setLayout(new BorderLayout());
        welcomeDialog.setSize(350, 200);
        welcomeDialog.setLocationRelativeTo(frame);
        welcomeDialog.getContentPane().setBackground(Color.WHITE);
        
        JLabel welcomeLabel = new JLabel("<html><div style='text-align: center;'><h2>Welcome, " + username + "!</h2><p>You have successfully logged in as <b>" + role + "</b>.</p></div>", JLabel.CENTER);
        welcomeLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JButton okButton = new JButton("Get Started");
        okButton.setBackground(new Color(52, 152, 219));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);
        okButton.setBorderPainted(false);
        okButton.setOpaque(true);
        okButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        okButton.addActionListener(e -> welcomeDialog.dispose());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        buttonPanel.add(okButton);
        
        welcomeDialog.add(welcomeLabel, BorderLayout.CENTER);
        welcomeDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show dialog in a separate thread to avoid blocking
        new Thread(() -> {
            try {
                Thread.sleep(100); // Small delay to ensure dialog is properly initialized
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            welcomeDialog.setVisible(true);
        }).start();
    }

    // LOGOUT FUNCTION
    private void logout() {
        // Custom logout confirmation dialog
        JDialog logoutDialog = new JDialog(frame, "Logout", true);
        logoutDialog.setLayout(new BorderLayout());
        logoutDialog.setSize(350, 180);
        logoutDialog.setLocationRelativeTo(frame);
        logoutDialog.getContentPane().setBackground(Color.WHITE);
        
        JLabel messageLabel = new JLabel("<html><div style='text-align: center;'><h3>Logout Confirmation</h3><p>Are you sure you want to log out?</p></div>", JLabel.CENTER);
        messageLabel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        JButton yesButton = new JButton("Yes, Logout");
        yesButton.setBackground(new Color(231, 76, 60));
        yesButton.setForeground(Color.WHITE);
        yesButton.setFocusPainted(false);
        yesButton.setBorderPainted(false);
        yesButton.setOpaque(true);
        yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesButton.addActionListener(e -> {
            performLogout();
            logoutDialog.dispose();
        });
        
        JButton noButton = new JButton("Cancel");
        noButton.setBackground(new Color(149, 165, 166));
        noButton.setForeground(Color.WHITE);
        noButton.setFocusPainted(false);
        noButton.setBorderPainted(false);
        noButton.setOpaque(true);
        noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noButton.addActionListener(e -> logoutDialog.dispose());
        
        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        
        logoutDialog.add(messageLabel, BorderLayout.CENTER);
        logoutDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        logoutDialog.setVisible(true);
    }
    
    private void performLogout() {
        // Clear user data
        userLabel.setText("");
        btnLogout.setEnabled(false);

        // Disable panel controls
        healthPanel.setControlsEnabled(false);
        
        // Disable all tabs
        for (int i = 0; i < tabs.getTabCount(); i++) {
            tabs.setEnabledAt(i, false);
        }

        // Switch to login panel
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, "login");
        
        // Clear login fields
        loginPanel = new LoginPanel(this);
        mainPanel.remove(0); // Remove old login panel
        mainPanel.add(loginPanel, "login", 0); // Add new login panel at index 0

        JOptionPane.showMessageDialog(frame, "You have been logged out securely.");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(PoultryApp::new);
    }
}
