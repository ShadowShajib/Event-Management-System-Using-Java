import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class RegisterForm extends JFrame {
    private JTextField nameField, emailField;
    private JPasswordField passwordField;
    private JComboBox<String> roleBox;

    public RegisterForm() {
        setTitle("Register");
        setSize(300, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(null);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(20, 20, 80, 25);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(100, 20, 160, 25);
        add(nameField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(20, 60, 80, 25);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(100, 60, 160, 25);
        add(emailField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(20, 100, 80, 25);
        add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(100, 100, 160, 25);
        add(passwordField);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(20, 140, 80, 25);
        add(roleLabel);

        roleBox = new JComboBox<>(new String[]{"user", "admin"});
        roleBox.setBounds(100, 140, 160, 25);
        add(roleBox);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(90, 190, 100, 25);
        add(registerButton);

        registerButton.addActionListener(e -> registerUser());

        setVisible(true);
    }

    private void registerUser() {
    String name = nameField.getText().trim();
    String email = emailField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();
    String role = (String) roleBox.getSelectedItem();

    if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role == null) {
        JOptionPane.showMessageDialog(this,
                "All fields are required!",
                "Input Error",
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    if (!email.matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
        JOptionPane.showMessageDialog(this,
                "Please enter a valid Gmail address (e.g. user@gmail.com)",
                "Invalid Email",
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    try (Connection conn = DBConnection.getConnection()) {
        String sql = "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, name);
        stmt.setString(2, email);
        stmt.setString(3, password);
        stmt.setString(4, role);

        int rows = stmt.executeUpdate();
        if (rows > 0) {
            JOptionPane.showMessageDialog(this, "Registration successful!");
            dispose();
            new LoginForm();
        }
    } catch (SQLException e) {
        if (e.getMessage().contains("Duplicate")) {
            JOptionPane.showMessageDialog(this,
                    "Email already registered!",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
  }
}
