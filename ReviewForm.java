import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class ReviewForm extends JFrame {

    private JTable table;
    private DefaultTableModel model;

    public ReviewForm(String username, JFrame parent) {
        setTitle("My Completed Events - Reviews");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultTableModel(new String[]{"ID", "Venue", "Event", "Date", "Status", "Action"}, 0);
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        loadCompletedRequests(username);

        TableColumn actionColumn = table.getColumn("Action");
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox(), requestId -> openReviewDialog(requestId, username)));

        JButton backBtn = new JButton("← Back");
        backBtn.setBackground(new Color(70, 130, 180));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setPreferredSize(new Dimension(100, 40));
        backBtn.addActionListener(e -> {
            this.setVisible(false);
            parent.setVisible(true);
        });

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(backBtn, BorderLayout.WEST);

        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadCompletedRequests(String username) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT er.id, v.name AS venue, e.name AS event, er.event_date, er.status, r.id AS review_id " +
                    "FROM event_requests er " +
                    "JOIN venues v ON er.venue_id = v.id " +
                    "JOIN events e ON er.event_id = e.id " +
                    "LEFT JOIN reviews r ON er.id = r.request_id " +
                    "WHERE er.username = ? AND er.status = 'Completed'";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("id"));
                row.add(rs.getString("venue"));
                row.add(rs.getString("event"));
                row.add(rs.getDate("event_date"));
                row.add(rs.getString("status"));

                if (rs.getObject("review_id") != null) {
                    row.add("Reviewed"); 
                } else {
                    row.add("Review"); 
                }

                model.addRow(row);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading requests: " + ex.getMessage());
        }
    }

    private boolean hasExistingReview(int requestId) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "SELECT id FROM reviews WHERE request_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, requestId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

    private void openReviewDialog(int requestId, String username) {
        if (hasExistingReview(requestId)) {
            JOptionPane.showMessageDialog(this, "You have already submitted a review for this event!");
            model.setValueAt("Reviewed", findRowByRequestId(requestId), 5);
            return;
        }

        JDialog dialog = new JDialog(this, "Submit Review", true);
        dialog.setSize(400, 250);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel starLabel = new JLabel("Star:");
        JComboBox<Integer> starCombo = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5});

        JLabel reviewLabel = new JLabel("Write:");
        JTextArea reviewText = new JTextArea(3, 20);

        panel.add(starLabel);
        panel.add(starCombo);
        panel.add(reviewLabel);
        panel.add(new JScrollPane(reviewText));

        JButton submitBtn = new JButton("Submit Review");
        submitBtn.addActionListener(e -> {
            if (hasExistingReview(requestId)) {
                JOptionPane.showMessageDialog(this, "You have already submitted a review for this event!");
                model.setValueAt("Reviewed", findRowByRequestId(requestId), 5);
                dialog.dispose();
                return;
            }

            int stars = (int) starCombo.getSelectedItem();
            String review = reviewText.getText();

            saveReviewToDatabase(requestId, username, stars, review);

            JOptionPane.showMessageDialog(this, "Review submitted successfully!");
            dialog.dispose();
            model.setValueAt("Reviewed", findRowByRequestId(requestId), 5);
        });

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(submitBtn, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private void saveReviewToDatabase(int requestId, String username, int stars, String review) {
        try (Connection conn = DBConnection.getConnection()) {
            String query = "INSERT INTO reviews (request_id, username, stars, comment) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, requestId);
            stmt.setString(2, username);
            stmt.setInt(3, stars);
            stmt.setString(4, review);
            stmt.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving review: " + ex.getMessage());
        }
    }

    private int findRowByRequestId(int requestId) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((int) model.getValueAt(i, 0) == requestId) {
                return i;
            }
        }
        return -1;
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            setEnabled(!"Reviewed".equals(value));
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int requestId;
        private java.util.function.Consumer<Integer> callback;

        public ButtonEditor(JCheckBox checkBox, java.util.function.Consumer<Integer> callback) {
            super(checkBox);
            this.callback = callback;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            requestId = (int) table.getValueAt(row, 0);
            button.setText(label);
            clicked = true;
            return button;
        }

        public Object getCellEditorValue() {
            if (clicked && "Review".equals(label)) {
                callback.accept(requestId);
            }
            clicked = false;
            return label;
        }

        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
