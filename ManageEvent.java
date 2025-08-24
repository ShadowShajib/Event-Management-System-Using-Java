import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageEvent extends JFrame {
    private JComboBox<VenueItem> venueComboBox;
    private JTextField eventNameField, eventCostField;
    private JTable eventTable;
    private DefaultTableModel tableModel;
    private AdminDashboard adminDashboard;

    public ManageEvent(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;

        setTitle("Manage Events");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Update Event"));

        formPanel.add(new JLabel("Select Venue:"));
        venueComboBox = new JComboBox<>();
        formPanel.add(venueComboBox);

        formPanel.add(new JLabel("Event Name:"));
        eventNameField = new JTextField();
        formPanel.add(eventNameField);

        formPanel.add(new JLabel("Event Cost:"));
        eventCostField = new JTextField();
        formPanel.add(eventCostField);

        JButton addButton = new JButton("Add Event");
        JButton updateButton = new JButton("Update Event");
        formPanel.add(addButton);
        formPanel.add(updateButton);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Event ID", "Event Name", "Cost"}, 0);
        eventTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(eventTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        JButton deleteButton = new JButton("Delete Event");

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftPanel.add(backButton);
        rightPanel.add(deleteButton);

        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);

        loadVenues();

        venueComboBox.addActionListener(e -> {
            VenueItem selectedVenue = (VenueItem) venueComboBox.getSelectedItem();
            if (selectedVenue != null) loadEvents(selectedVenue.id);
        });

        eventTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = eventTable.getSelectedRow();
                if (row >= 0) {
                    eventNameField.setText(tableModel.getValueAt(row, 1).toString());
                    eventCostField.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        addButton.addActionListener(e -> addEvent());
        updateButton.addActionListener(e -> updateEvent());
        deleteButton.addActionListener(e -> deleteEvent());

        backButton.addActionListener(e -> {
            dispose();
            adminDashboard.setVisible(true);
        });

        setVisible(true);
    }

    private void loadVenues() {
        venueComboBox.removeAllItems();
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, name FROM venues";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                venueComboBox.addItem(new VenueItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading venues.");
        }
    }

    private void loadEvents(int venueId) {
        tableModel.setRowCount(0);
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, name, cost FROM events WHERE venue_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, venueId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getDouble("cost")
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events.");
        }
    }

    private void addEvent() {
        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) {
            JOptionPane.showMessageDialog(this, "Select a venue.");
            return;
        }

        String name = eventNameField.getText().trim();
        String costStr = eventCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "INSERT INTO events (venue_id, name, cost) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, venue.id);
                ps.setString(2, name);
                ps.setDouble(3, cost);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Event added!");
                loadEvents(venue.id);
                eventNameField.setText("");
                eventCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding event.");
        }
    }

    private void updateEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event to update.");
            return;
        }

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String name = eventNameField.getText().trim();
        String costStr = eventCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "UPDATE events SET name = ?, cost = ? WHERE id = ? AND venue_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDouble(2, cost);
                ps.setInt(3, id);
                ps.setInt(4, venue.id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Event updated.");
                loadEvents(venue.id);
                eventNameField.setText("");
                eventCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating event.");
        }
    }

    private void deleteEvent() {
        int row = eventTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        try (Connection conn = DB.getConnection()) {
            String sql = "DELETE FROM events WHERE id = ? AND venue_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, venue.id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Event deleted.");
            loadEvents(venue.id);
            eventNameField.setText("");
            eventCostField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting event.");
        }
    }

    private static class VenueItem {
        int id;
        String name;

        public VenueItem(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }
}
