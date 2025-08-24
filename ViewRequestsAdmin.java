import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.*;

public class ViewRequestsAdmin extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private AdminDashboard parent;

    public ViewRequestsAdmin(AdminDashboard parent) {
        this.parent = parent;

        setTitle("View Booking Requests");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        model = new DefaultTableModel(new String[]{
                "ID", "Username", "Venue", "Event", "Date", "Guests",
                "Foods", "Equipment", "Event Cost", "Food Cost", "Equipment Cost",
                "Total", "Status"
        }, 0);

        table = new JTable(model);

        table.setRowHeight(35);

        loadRequests();

        String[] statuses = {"Pending", "Accepted", "Rejected", "Completed"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        table.getColumnModel().getColumn(12).setCellEditor(new DefaultCellEditor(statusCombo));

        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 12) {
                int row = e.getFirstRow();
                int id = Integer.parseInt(model.getValueAt(row, 0).toString());
                String newStatus = model.getValueAt(row, 12).toString();
                updateStatus(id, newStatus);
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        JButton backBtn = new JButton("← Back");
        backBtn.setPreferredSize(new Dimension(100, 35));
        backBtn.addActionListener(e -> {
            this.dispose();
            parent.setVisible(true);
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);
        setVisible(true);
    }

    private void loadRequests() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event_management", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT er.id, er.username, v.name AS venue_name, e.name AS event_name, " +
                             "er.event_date, er.guests, er.food_ids, er.equipment_ids, " +
                             "er.event_cost, er.food_cost, er.equipment_cost, er.total_cost, er.status " +
                             "FROM event_requests er " +
                             "JOIN venues v ON er.venue_id = v.id " +
                             "JOIN events e ON er.event_id = e.id"
             )) {

            while (rs.next()) {
                String foods = getNamesFromIds(conn, "foods", rs.getString("food_ids"));
                String equipment = getNamesFromIds(conn, "equipment", rs.getString("equipment_ids"));

                model.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("venue_name"),
                        rs.getString("event_name"),
                        rs.getDate("event_date"),
                        rs.getInt("guests"),
                        foods,
                        equipment,
                        rs.getDouble("event_cost"),
                        rs.getDouble("food_cost"),
                        rs.getDouble("equipment_cost"),
                        rs.getDouble("total_cost"),
                        rs.getString("status")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getNamesFromIds(Connection conn, String tableName, String ids) {
        if (ids == null || ids.trim().isEmpty()) return "";
        StringBuilder names = new StringBuilder();
        try {
            String[] idArray = ids.split(",");
            for (String idStr : idArray) {
                try (PreparedStatement ps = conn.prepareStatement("SELECT name FROM " + tableName + " WHERE id=?")) {
                    ps.setInt(1, Integer.parseInt(idStr.trim()));
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            if (names.length() > 0) names.append(", ");
                            names.append(rs.getString("name"));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return names.toString();
    }

    private void updateStatus(int id, String newStatus) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/event_management", "root", "");
             PreparedStatement ps = conn.prepareStatement("UPDATE event_requests SET status=? WHERE id=?")) {
            ps.setString(1, newStatus);
            ps.setInt(2, id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Status updated to: " + newStatus);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
