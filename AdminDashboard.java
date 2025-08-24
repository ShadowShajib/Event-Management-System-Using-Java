import javax.swing.*;
import java.awt.*;

public class AdminDashboard extends JFrame {

    private String adminName;

    public AdminDashboard(String name) {
        this.adminName = name;

        setTitle("Admin Dashboard");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JLabel welcomeLabel = new JLabel("Welcome, " + name, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(welcomeLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton manageVenueBtn = createStyledButton("Manage Venues");
        JButton manageEventBtn = createStyledButton("Manage Events");
        JButton manageFoodBtn = createStyledButton("Manage Foods");
        JButton manageEquipBtn = createStyledButton("Manage Equipment");
        JButton viewRequestsBtn = createStyledButton("View Booking Requests");
        JButton logoutBtn = createStyledButton("Logout");

        buttonPanel.add(manageVenueBtn);
        buttonPanel.add(manageEventBtn);
        buttonPanel.add(manageFoodBtn);
        buttonPanel.add(manageEquipBtn);
        buttonPanel.add(viewRequestsBtn);
        buttonPanel.add(logoutBtn);

        add(buttonPanel, BorderLayout.CENTER);

        manageVenueBtn.addActionListener(e -> {
            this.setVisible(false);
            new ManageVenue(this);
        });

        manageEventBtn.addActionListener(e -> {
            this.setVisible(false);
            new ManageEvent(this);
        });

        manageFoodBtn.addActionListener(e -> {
            this.setVisible(false);
            new ManageFood(this);
        });

        manageEquipBtn.addActionListener(e -> {
            this.setVisible(false);
            new ManageEquipment(this);
        });

        viewRequestsBtn.addActionListener(e -> {
            this.setVisible(false);
            new ViewRequestsAdmin(this);
        });

        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginForm();
        });

        setVisible(true);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(180, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 149, 237));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });

        return button;
    }
}
