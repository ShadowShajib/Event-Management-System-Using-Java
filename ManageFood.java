import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ManageFood extends JFrame {
    private JComboBox<VenueItem> venueComboBox;
    private JTextField foodNameField, foodCostField;
    private JTable foodTable;
    private DefaultTableModel tableModel;
    private AdminDashboard adminDashboard;

    public ManageFood(AdminDashboard adminDashboard) {
        this.adminDashboard = adminDashboard;

        setTitle("Manage Food");
        setSize(750, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createTitledBorder("Add / Update Food"));

        formPanel.add(new JLabel("Select Venue:"));
        venueComboBox = new JComboBox<>();
        formPanel.add(venueComboBox);

        formPanel.add(new JLabel("Food Name:"));
        foodNameField = new JTextField();
        formPanel.add(foodNameField);

        formPanel.add(new JLabel("Food Cost:"));
        foodCostField = new JTextField();
        formPanel.add(foodCostField);

        JButton addButton = new JButton("Add Food");
        JButton updateButton = new JButton("Update Food");
        formPanel.add(addButton);
        formPanel.add(updateButton);

        add(formPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"Food ID", "Food Name", "Cost"}, 0);
        foodTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(foodTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton backButton = new JButton("← Back");
        JButton deleteButton = new JButton("Delete Food");

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
            if (selectedVenue != null) loadFoods(selectedVenue.id);
        });

        foodTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = foodTable.getSelectedRow();
                if (row >= 0) {
                    foodNameField.setText(tableModel.getValueAt(row, 1).toString());
                    foodCostField.setText(tableModel.getValueAt(row, 2).toString());
                }
            }
        });

        addButton.addActionListener(e -> addFood());
        updateButton.addActionListener(e -> updateFood());
        deleteButton.addActionListener(e -> deleteFood());

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

    private void loadFoods(int venueId) {
        tableModel.setRowCount(0);
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT id, name, cost FROM foods WHERE venue_id = ?";
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
            JOptionPane.showMessageDialog(this, "Error loading foods.");
        }
    }

    private void addFood() {
        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) {
            JOptionPane.showMessageDialog(this, "Select a venue.");
            return;
        }

        String name = foodNameField.getText().trim();
        String costStr = foodCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "INSERT INTO foods (venue_id, name, cost) VALUES (?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, venue.id);
                ps.setString(2, name);
                ps.setDouble(3, cost);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Food added!");
                loadFoods(venue.id);
                foodNameField.setText("");
                foodCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding food.");
        }
    }

    private void updateFood() {
        int row = foodTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a food to update.");
            return;
        }

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        if (venue == null) return;

        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
        String name = foodNameField.getText().trim();
        String costStr = foodCostField.getText().trim();

        if (name.isEmpty() || costStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fill in all fields.");
            return;
        }

        try {
            double cost = Double.parseDouble(costStr);
            try (Connection conn = DB.getConnection()) {
                String sql = "UPDATE foods SET name = ?, cost = ? WHERE id = ? AND venue_id = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, name);
                ps.setDouble(2, cost);
                ps.setInt(3, id);
                ps.setInt(4, venue.id);
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Food updated.");
                loadFoods(venue.id);
                foodNameField.setText("");
                foodCostField.setText("");
            }
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Invalid cost.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating food.");
        }
    }

    private void deleteFood() {
        int row = foodTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select a food to delete.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure?", "Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        int id = Integer.parseInt(tableModel.getValueAt(row, 0).toString());

        try (Connection conn = DB.getConnection()) {
            String sql = "DELETE FROM foods WHERE id = ? AND venue_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, venue.id);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Food deleted.");
            loadFoods(venue.id);
            foodNameField.setText("");
            foodCostField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting food.");
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
