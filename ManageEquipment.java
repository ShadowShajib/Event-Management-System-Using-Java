import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageEquipment extends JFrame {
    private JComboBox<VenueItem> venueComboBox;
    private JTextField equipmentNameField, equipmentCostField;
    private JTable equipmentTable;
    private DefaultTableModel tableModel;
    private AdminDashboard adminDashboard;

    public ManageEquipment(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;

        setTitle("Manage Equipment");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Update Equipment"));

        formPanel.add(new JLabel("Select Venue:"));
        venueComboBox = new JComboBox<>();
        formPanel.add(venueComboBox);

        formPanel.add(new JLabel("Equipment Name:"));
        equipmentNameField = new JTextField();
        formPanel.add(equipmentNameField);

        formPanel.add(new JLabel("Equipment Cost:"));
        equipmentCostField = new JTextField();
        formPanel.add(equipmentCostField);

        JButton addButton = new JButton("Add Equipment");
        JButton updateButton = new JButton("Update Equipment");
        formPanel.add(addButton);
        formPanel.add(updateButton);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Cost"}, 0);
        equipmentTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(equipmentTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        JButton deleteButton = new JButton("Delete Equipment");

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
            if (selectedVenue != null) loadEquipment(selectedVenue.id);
        });

        equipmentTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = equipmentTable.getSelectedRow();
                if (row >= 0) {
                    equipmentNameField.setText(tableModel.getValueAt(row, 1).toString());
                    equipmentCostField.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        addButton.addActionListener(e -> addEquipment());
        updateButton.addActionListener(e -> updateEquipment());
        deleteButton.addActionListener(e -> deleteEquipment());

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

    private void loadEquipment(int venueId) {
        tableModel.setRowCount(0);
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, name, cost FROM equipment WHERE venue_id = ?";
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
            JOptionPane.showMessageDialog(this, "Error loading equipment.");
        }
    }

    private void addEquipment() {
        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) {
            JOptionPane.showMessageDialog(this, "Select a venue.");
            return;
        }

        String name = equipmentNameField.getText().trim();
        String costStr = equipmentCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "INSERT INTO equipment (venue_id, name, cost) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, venue.id);
                ps.setString(2, name);
                ps.setDouble(3, cost);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Equipment added!");
                loadEquipment(venue.id);
                equipmentNameField.setText("");
                equipmentCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding equipment.");
        }
    }

    private void updateEquipment() {
        int row = equipmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an equipment to update.");
            return;
        }

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String name = equipmentNameField.getText().trim();
        String costStr = equipmentCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "UPDATE equipment SET name = ?, cost = ? WHERE id = ? AND venue_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDouble(2, cost);
                ps.setInt(3, id);
                ps.setInt(4, venue.id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Equipment updated.");
                loadEquipment(venue.id);
                equipmentNameField.setText("");
                equipmentCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating equipment.");
        }
    }

    private void deleteEquipment() {
        int row = equipmentTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an equipment to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        try (Connection conn = DB.getConnection()) {
            String sql = "DELETE FROM equipment WHERE id = ? AND venue_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, venue.id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Equipment deleted.");
            loadEquipment(venue.id);
            equipmentNameField.setText("");
            equipmentCostField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting equipment.");
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
