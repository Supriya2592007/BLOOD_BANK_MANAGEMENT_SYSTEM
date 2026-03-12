import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HospitalDashboard extends JFrame {
    private int hospitalId;
    private String hospitalName;
    private JPanel contentPanel;

    public HospitalDashboard(int hId, String hName) {
        this.hospitalId = hId;
        this.hospitalName = hName;

        setTitle("BBMS - Hospital Portal");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // Sidebar
        JPanel sidebar = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 50, 50)); 
                g2.fillRoundRect(20, 20, 220, 720, 30, 30);
            }
        };
        sidebar.setBounds(0, 0, 260, 800);
        sidebar.setOpaque(false);
        add(sidebar);

        JLabel lblPortal = new JLabel("HOSPITAL PORTAL", SwingConstants.CENTER);
        lblPortal.setBounds(20, 60, 220, 40);
        lblPortal.setFont(new Font("Arial", Font.BOLD, 18));
        lblPortal.setForeground(Color.WHITE);
        sidebar.add(lblPortal);

        // Main Content Area
        contentPanel = new JPanel(null);
        contentPanel.setBounds(260, 0, 940, 800);
        contentPanel.setBackground(new Color(245, 245, 250));
        add(contentPanel);

        // --- BUTTONS STYLED LIKE ADMIN PANEL ---
        String[] menuItems = {"Previous Transactions", "Request Blood", "Contact Staff", "Logout"};
        int yPos = 240; // Adjusted starting position
        for (String item : menuItems) {
            JButton btn = createAdminStyleButton(item);
            btn.setBounds(40, yPos, 180, 45);
            sidebar.add(btn);
            yPos += 70;

            btn.addActionListener(e -> {
                if (item.equals("Logout")) {
                    dispose();
                    new MainWindow().setVisible(true);
                } else {
                    switchContent(item);
                }
            });
        }

        switchContent("Previous Transactions"); // Initial view
    }

    private void switchContent(String view) {
        contentPanel.removeAll();
        
        JLabel lblHeader = new JLabel(view.toUpperCase());
        lblHeader.setBounds(50, 50, 600, 50);
        lblHeader.setFont(new Font("Arial", Font.BOLD, 32));
        lblHeader.setForeground(new Color(120, 0, 0));
        contentPanel.add(lblHeader);

        if (view.equals("Previous Transactions")) {
            showTransactionsTable();
        } else if (view.equals("Request Blood")) {
            showRequestForm();
        } else if (view.equals("Contact Staff")) {
            showStaffContact();
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // --- LOGIC FOR "PREVIOUS TRANSACTIONS" ---
    private void showTransactionsTable() {
        String[] columns = {"ID", "Blood Group", "Units", "Status", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setRowHeight(30);
        
        try (Connection conn = DbConnection.connect()) {
            String sql = "SELECT request_id, blood_group, units, status, request_date FROM Hospital_blood_requests WHERE hospital_id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, hospitalId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDate(5)});
            }
        } catch (Exception e) { e.printStackTrace(); }

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 150, 820, 500);
        contentPanel.add(sp);
    }

    // --- LOGIC FOR "REQUEST BLOOD" ---
    private void showRequestForm() {
        String[] groups = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"};
        JComboBox<String> comboBG = new JComboBox<>(groups);
        JTextField txtUnits = new JTextField();
        JButton btnSubmit = new JButton("Submit Request");

        comboBG.setBounds(250, 150, 200, 35);
        txtUnits.setBounds(250, 210, 200, 35);
        btnSubmit.setBounds(250, 280, 200, 40);
        btnSubmit.setBackground(new Color(160, 25, 25));
        btnSubmit.setForeground(Color.WHITE);

        contentPanel.add(new JLabel("Blood Group:")).setBounds(100, 150, 100, 35);
        contentPanel.add(new JLabel("Units Required:")).setBounds(100, 210, 100, 35);
        contentPanel.add(comboBG);
        contentPanel.add(txtUnits);
        contentPanel.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
    String bloodGroup = (String) comboBG.getSelectedItem();
    String unitsStr = txtUnits.getText();

    // Basic validation
    if (unitsStr.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter the number of units.");
        return;
    }

    try {
        int units = Integer.parseInt(unitsStr);

        try (Connection conn = DbConnection.connect()) {
            // SQL to insert the request using the logged-in hospital's ID
            String sql = "INSERT INTO Hospital_blood_requests (hospital_id, blood_group, units, status, request_date) VALUES (?, ?, ?, 'Pending', CURDATE())";
            PreparedStatement ps = conn.prepareStatement(sql);
            
            ps.setInt(1, hospitalId);      // Uses the ID passed during login
            ps.setString(2, bloodGroup);   // From the dropdown menu
            ps.setInt(3, units);           // From the text field
            
            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Request Submitted Successfully!");
                txtUnits.setText(""); // Clear field after success
                switchContent("Previous Transactions"); // Refresh the table view
            }
        }
    } catch (NumberFormatException nfe) {
        JOptionPane.showMessageDialog(this, "Please enter a valid number for units.");
    } catch (SQLException ex) {
        JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
    }
});
    }

    // --- LOGIC FOR "CONTACT STAFF" ---
   // --- UPDATED LOGIC FOR "CONTACT STAFF" ---
    private void showStaffContact() {
        // Table setup with specific columns
        String[] cols = {"Staff Name", "Contact Number"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = new JTable(model);
        
        // Applying visual styling to match User Portal
        table.setRowHeight(35);
        table.getTableHeader().setBackground(new Color(160, 25, 25));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        // Loading Admin data from the database
        try (Connection conn = DbConnection.connect()) {
            // Fetching only 'Admin' roles to match your requirement
            String sql = "SELECT name, phone FROM users WHERE role = 'Admin'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"), 
                    rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading staff data: " + e.getMessage());
        }

        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(50, 150, 820, 400); // Matching layout from image_ab3a31.png
        contentPanel.add(sp);
    }

    // --- HELPER TO CREATE THE SEMI-TRANSPARENT ROUNDED BUTTONS ---
    private JButton createAdminStyleButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 40)); // Semi-transparent white
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        return btn;
    }
}
