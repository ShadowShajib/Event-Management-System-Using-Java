import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageVenue extends JFrame {
    private JTextField nameField, locationField;
    private JTable venueTable;
    private DefaultTableModel tableModel;
    private AdminDashboard adminDashboard; 

    public ManageVenue(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;

        setTitle("Manage Venues");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Venue Details"));

        inputPanel.add(new JLabel("Venue Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Location:"));
        locationField = new JTextField();
        inputPanel.add(locationField);

        JButton addButton = new JButton("Add Venue");
        JButton updateButton = new JButton("Update Venue");

        inputPanel.add(addButton);
        inputPanel.add(updateButton);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Location"}, 0);
        venueTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(venueTable);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JButton backButton = new JButton("← Back");
        JButton deleteButton = new JButton("Delete Venue");

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        leftPanel.add(backButton);
        rightPanel.add(deleteButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        add(inputPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        loadVenues();

        venueTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = venueTable.getSelectedRow();
                if (row >= 0) {
                    nameField.setText(tableModel.getValueAt(row, 1).toString());
                    locationField.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        addButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();

            if (name.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            try (Connection conn = DB.getConnection()) {
                String query = "INSERT INTO venues (name, location) VALUES (?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, location);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Venue added!");
                loadVenues();
                nameField.setText("");
                locationField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        updateButton.addActionListener(e -> {
            int row = venueTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a venue to update.");
                return;
            }

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            String name = nameField.getText().trim();
            String location = locationField.getText().trim();

            if (name.isEmpty() || location.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.");
                return;
            }

            try (Connection conn = DB.getConnection()) {
                String query = "UPDATE venues SET name = ?, location = ? WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, location);
                ps.setInt(3, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Venue updated!");
                loadVenues();
                nameField.setText("");
                locationField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        deleteButton.addActionListener(e -> {
            int row = venueTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a venue to delete.");
                return;
            }

            int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure to delete?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) return;

            try (Connection conn = DB.getConnection()) {
                String query = "DELETE FROM venues WHERE id = ?";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setInt(1, id);
                ps.executeUpdate();

                JOptionPane.showMessageDialog(this, "Venue deleted!");
                loadVenues();
                nameField.setText("");
                locationField.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        backButton.addActionListener(e -> {
            this.dispose();    
            adminDashboard.setVisible(true);
        });

        setVisible(true);
    }

    private void loadVenues() {
        tableModel.setRowCount(0);

        try (Connection conn = DB.getConnection()) {
            String query = "SELECT * FROM venues";
            PreparedStatement ps = conn.prepareStatement(query);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Object[] row = {
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("location")
                };
                tableModel.addRow(row);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading venues: " + ex.getMessage());
        }
    }
}
