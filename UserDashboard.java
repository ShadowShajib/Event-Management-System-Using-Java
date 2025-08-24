import javax.swing.*;
import java.awt.*;

public class UserDashboard extends JFrame {

    private String username;

    public UserDashboard(String username) {
        this.username = username;
        setTitle("User Dashboard");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(240, 240, 240));
        add(mainPanel);
 
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(240, 240, 240)); 
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.BLACK); 
        topPanel.add(welcomeLabel);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));
        centerPanel.setBackground(new Color(240, 240, 240));

        JButton btnRequestEvent = createStyledButton("Request an Event");
        JButton btnMyRequests = createStyledButton("My Requests");
        JButton btnGiveReviews = createStyledButton("Give Reviews");
        JButton btnLogout = createStyledButton("Logout");

        btnRequestEvent.addActionListener(e -> {
        this.setVisible(false);
        new RequestEventForm(username).setVisible(true);
        });

        btnMyRequests.addActionListener(e -> {
        this.setVisible(false);   
        new MyRequests(username).setVisible(true);    
        });

        btnGiveReviews.addActionListener(e -> {
        this.setVisible(false);   
        new ReviewForm(username, this);
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });

        centerPanel.add(btnRequestEvent);
        centerPanel.add(btnMyRequests);
        centerPanel.add(btnGiveReviews);
        centerPanel.add(btnLogout);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(180, 50));

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new UserDashboard("User123").setVisible(true));
    }
}
