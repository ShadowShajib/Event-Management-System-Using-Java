import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class MyRequests extends JFrame {
    private String username;
    private JTable table;
    private DefaultTableModel tableModel;

    public MyRequests(String username) {
        this.username = username;

        setTitle("My Event Requests");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(
                new String[]{"ID", "Venue", "Event", "Date", "Guests", "Status", "Total Cost"}, 0
        );
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); 
        JButton backButton = new JButton("← Back");
        backButton.setPreferredSize(new Dimension(100, 30)); 
        backButton.setBackground(new Color(70, 130, 180)); 
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        bottomPanel.add(backButton);
        add(bottomPanel, BorderLayout.SOUTH);

        loadRequests();

        backButton.addActionListener((ActionEvent e) -> {
            new UserDashboard(username).setVisible(true);
            dispose();
        });
    }

    private void loadRequests() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/event_management", "root", "")) {

            String sql = "SELECT er.id, v.name AS venue, e.name AS event, er.event_date, " +
                         "er.guests, er.status, er.total_cost " +
                         "FROM event_requests er " +
                         "JOIN venues v ON er.venue_id = v.id " +
                         "JOIN events e ON er.event_id = e.id " +
                         "WHERE er.username = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            tableModel.setRowCount(0);

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("venue"),
                        rs.getString("event"),
                        rs.getDate("event_date"),
                        rs.getInt("guests"),
                        rs.getString("status"),
                        rs.getDouble("total_cost")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading requests: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MyRequests("testuser").setVisible(true));
    }
}
