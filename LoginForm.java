import javax.swing.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public LoginForm() {
        setTitle("Login");
        setSize(350, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(20, 30, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(100, 30, 200, 25);
        add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 70, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 70, 200, 25);
        add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(20, 110, 80, 25);
        add(roleLabel);

        roleBox = new JComboBox<>(new String[]{"user", "admin"});
        roleBox.setBounds(100, 110, 200, 25);
        add(roleBox);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(120, 160, 100, 25);
        add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(120, 195, 100, 25);
        add(registerButton);

        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> {
            dispose();
            new RegisterForm();
        });

        setVisible(true);
    }

    private void loginUser() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String selectedRole = (String) roleBox.getSelectedItem();

        if (email.isEmpty() || password.isEmpty() || selectedRole == null) {
            JOptionPane.showMessageDialog(this,
                    "Please fill in all fields and select a role.",
                    "Missing Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE email = ? AND password = ? AND role = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password);
            stmt.setString(3, selectedRole);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String role = rs.getString("role");

                JOptionPane.showMessageDialog(this,
                        "Welcome, " + name + "! You are logged in as: " + role.toUpperCase());

                if (role.equalsIgnoreCase("admin")) {
                    new AdminDashboard(name).setVisible(true);
                } else {
                    new UserDashboard(name).setVisible(true);
                }

                dispose();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid email, password, or role.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
