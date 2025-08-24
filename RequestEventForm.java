import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;

public class RequestEventForm extends JFrame {
    private JComboBox<VenueItem> venueComboBox;
    private JComboBox<EventItem> eventComboBox;
    private JList<FoodItem> foodList;
    private JList<EquipmentItem> equipmentList;
    private JTextField guestField;
    private JSpinner dateSpinner;

    private DefaultTableModel selectionTableModel;
    private JTable selectionTable;

    private DefaultTableModel costTableModel;
    private JTable costTable;

    private JButton btnSubmitRequest, btnBack;
    private String username;

    public RequestEventForm(String username) {
        this.username = username;

        setTitle("Request an Event");
        setSize(920, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Event Request Details"));
        inputPanel.setBackground(new Color(245, 245, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Venue:"), gbc);
        venueComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 0; gbc.gridwidth = 2;
        inputPanel.add(venueComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Event:"), gbc);
        eventComboBox = new JComboBox<>();
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        inputPanel.add(eventComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Number of Guests:"), gbc);
        guestField = new JTextField();
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        inputPanel.add(guestField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Date:"), gbc);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        java.util.Date todayStart = cal.getTime();

        SpinnerDateModel sdm = new SpinnerDateModel(todayStart, todayStart, null, Calendar.DAY_OF_MONTH);
        dateSpinner = new JSpinner(sdm);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(editor);
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        inputPanel.add(dateSpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Food(s):"), gbc);
        foodList = new JList<>();
        foodList.setVisibleRowCount(4);
        foodList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane foodScroll = new JScrollPane(foodList);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        inputPanel.add(foodScroll, gbc);

        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        inputPanel.add(new JLabel("Select Equipment(s):"), gbc);
        equipmentList = new JList<>();
        equipmentList.setVisibleRowCount(4);
        equipmentList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        JScrollPane equipScroll = new JScrollPane(equipmentList);
        gbc.gridx = 1; gbc.gridy = 5; gbc.gridwidth = 2;
        inputPanel.add(equipScroll, gbc);

        add(inputPanel, BorderLayout.NORTH);

        selectionTableModel = new DefaultTableModel(new String[]{"Venue", "Event", "Date", "Guests", "Foods", "Equipment"}, 0);
        selectionTable = new JTable(selectionTableModel);
        JScrollPane selectionScroll = new JScrollPane(selectionTable);
        selectionScroll.setPreferredSize(new Dimension(900, 130));
        add(selectionScroll, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        costTableModel = new DefaultTableModel(new String[]{"Event Cost", "Food Cost", "Equipment Cost", "Total Cost"}, 1);
        costTableModel.setValueAt("0.00", 0, 0);
        costTableModel.setValueAt("0.00", 0, 1);
        costTableModel.setValueAt("0.00", 0, 2);
        costTableModel.setValueAt("0.00", 0, 3);
        costTable = new JTable(costTableModel);
        JScrollPane costScroll = new JScrollPane(costTable);
        costScroll.setPreferredSize(new Dimension(900, 60));
        bottomPanel.add(costScroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        btnSubmitRequest = createStyledButton("Submit");
        btnBack = createStyledButton("← Back");
        btnPanel.add(btnSubmitRequest);
        btnPanel.add(btnBack);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        loadVenues();
        loadFoods();
        loadEquipment();

        venueComboBox.addActionListener(e -> {
            VenueItem v = (VenueItem) venueComboBox.getSelectedItem();
            if (v != null) {
                loadEvents(v.id);
            } else {
                eventComboBox.removeAllItems();
            }
            updateSelectionAndCost();
        });

        eventComboBox.addActionListener(e -> updateSelectionAndCost());
        guestField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { updateSelectionAndCost(); }
        });
        foodList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateSelectionAndCost(); });
        equipmentList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) updateSelectionAndCost(); });
        dateSpinner.addChangeListener(e -> updateSelectionAndCost());

        btnSubmitRequest.addActionListener(e -> submitRequest(todayStart));
        btnBack.addActionListener(e -> {
            dispose();
            new UserDashboard(username).setVisible(true);
        });

        if (venueComboBox.getItemCount() > 0) {
            venueComboBox.setSelectedIndex(0);
        }
        updateSelectionAndCost();

        setVisible(true);
    }

    private void loadVenues() {
        venueComboBox.removeAllItems();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name FROM venues");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                venueComboBox.addItem(new VenueItem(rs.getInt("id"), rs.getString("name")));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading venues: " + ex.getMessage());
        }
    }

    private void loadEvents(int venueId) {
        eventComboBox.removeAllItems();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, cost FROM events WHERE venue_id = ?")) {
            ps.setInt(1, venueId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    eventComboBox.addItem(new EventItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("cost")));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading events: " + ex.getMessage());
        }
    }

    private void loadFoods() {
        DefaultListModel<FoodItem> model = new DefaultListModel<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, cost FROM foods");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addElement(new FoodItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("cost")));
            }
            foodList.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading foods: " + ex.getMessage());
        }
    }

    private void loadEquipment() {
        DefaultListModel<EquipmentItem> model = new DefaultListModel<>();
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id, name, cost FROM equipment");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                model.addElement(new EquipmentItem(rs.getInt("id"), rs.getString("name"), rs.getDouble("cost")));
            }
            equipmentList.setModel(model);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading equipment: " + ex.getMessage());
        }
    }

    private void updateSelectionAndCost() {
        selectionTableModel.setRowCount(0);

        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        EventItem event = (EventItem) eventComboBox.getSelectedItem();
        List<FoodItem> selectedFoods = foodList.getSelectedValuesList();
        List<EquipmentItem> selectedEquip = equipmentList.getSelectedValuesList();

        int guests = 0;
        try { guests = Integer.parseInt(guestField.getText().trim()); if (guests < 0) guests = 0; }
        catch (Exception ignored) {}

        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = sdf.format(selectedDate);

        String foodsStr = String.join(", ", toNames(selectedFoods));
        String equipStr = String.join(", ", toNamesEquip(selectedEquip));

        selectionTableModel.addRow(new Object[]{
                (venue != null ? venue.name : ""),
                (event != null ? event.name : ""),
                dateStr,
                guests,
                foodsStr,
                equipStr
        });

        double eventCost = (event != null ? event.cost : 0.0);
        double foodCost = 0.0;
        for (FoodItem f : selectedFoods) foodCost += f.cost * guests;
        double equipCost = 0.0;
        for (EquipmentItem eq : selectedEquip) equipCost += eq.cost;
        double totalCost = eventCost + foodCost + equipCost;

        costTableModel.setValueAt(String.format("%.2f", eventCost), 0, 0);
        costTableModel.setValueAt(String.format("%.2f", foodCost), 0, 1);
        costTableModel.setValueAt(String.format("%.2f", equipCost), 0, 2);
        costTableModel.setValueAt(String.format("%.2f", totalCost), 0, 3);
    }

    private List<String> toNames(List<FoodItem> list) {
        List<String> out = new ArrayList<>();
        for (FoodItem f : list) out.add(f.name);
        return out;
    }
    private List<String> toNamesEquip(List<EquipmentItem> list) {
        List<String> out = new ArrayList<>();
        for (EquipmentItem f : list) out.add(f.name);
        return out;
    }

    private void submitRequest(java.util.Date todayStart) {
        VenueItem venue = (VenueItem) venueComboBox.getSelectedItem();
        EventItem event = (EventItem) eventComboBox.getSelectedItem();
        List<FoodItem> selectedFoods = foodList.getSelectedValuesList();
        List<EquipmentItem> selectedEquip = equipmentList.getSelectedValuesList();

        int guests;
        try {
            guests = Integer.parseInt(guestField.getText().trim());
            if (guests <= 0) {
                JOptionPane.showMessageDialog(this, "Enter a valid number of guests (> 0).");
                return;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Enter a valid number of guests.");
            return;
        }

        java.util.Date selectedDate = (java.util.Date) dateSpinner.getValue();
        if (selectedDate.before(todayStart)) {
            JOptionPane.showMessageDialog(this, "Select a valid date (today or future).");
            return;
        }

        if (venue == null || event == null) {
            JOptionPane.showMessageDialog(this, "Please select a venue and an event.");
            return;
        }

        String foodIds = joinIds(selectedFoods);
        String equipIds = joinIdsEquip(selectedEquip);

        double eventCost = event.cost;
        double foodCost = 0.0;
        for (FoodItem f : selectedFoods) foodCost += f.cost * guests;
        double equipCost = 0.0;
        for (EquipmentItem e : selectedEquip) equipCost += e.cost;
        double totalCost = eventCost + foodCost + equipCost;

        String sql = "INSERT INTO event_requests (username, venue_id, event_id, event_date, guests, food_ids, equipment_ids, event_cost, food_cost, equipment_cost, total_cost, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setInt(2, venue.id);
            ps.setInt(3, event.id);
            ps.setDate(4, new java.sql.Date(selectedDate.getTime()));
            ps.setInt(5, guests);
            ps.setString(6, foodIds);
            ps.setString(7, equipIds);
            ps.setDouble(8, eventCost);
            ps.setDouble(9, foodCost);
            ps.setDouble(10, equipCost);
            ps.setDouble(11, totalCost);
            ps.setString(12, "Pending");

            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Request submitted successfully!");

            guestField.setText("");
            foodList.clearSelection();
            equipmentList.clearSelection();
            loadVenues();
            loadFoods();
            loadEquipment();
            updateSelectionAndCost();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting request: " + ex.getMessage());
        }
    }

    private String joinIds(List<FoodItem> list) {
        StringBuilder sb = new StringBuilder();
        for (FoodItem f : list) { if (sb.length() > 0) sb.append(","); sb.append(f.id); }
        return sb.toString();
    }
    private String joinIdsEquip(List<EquipmentItem> list) {
        StringBuilder sb = new StringBuilder();
        for (EquipmentItem f : list) { if (sb.length() > 0) sb.append(","); sb.append(f.id); }
        return sb.toString();
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(140, 40));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { button.setBackground(new Color(100,149,237)); }
            public void mouseExited(MouseEvent e) { button.setBackground(new Color(70,130,180)); }
        });
        return button;
    }

    private static class VenueItem { int id; String name; VenueItem(int i, String n){id=i;name=n;} public String toString(){return name;} }
    private static class EventItem  { int id; String name; double cost; EventItem(int i,String n,double c){id=i;name=n;cost=c;} public String toString(){return name + " ($" + cost + ")";} }
    private static class FoodItem   { int id; String name; double cost; FoodItem(int i,String n,double c){id=i;name=n;cost=c;} public String toString(){return name + " ($" + cost + ")";} }
    private static class EquipmentItem { int id; String name; double cost; EquipmentItem(int i,String n,double c){id=i;name=n;cost=c;} public String toString(){return name + " ($" + cost + ")";} }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RequestEventForm("demoUser"));
    }
}
