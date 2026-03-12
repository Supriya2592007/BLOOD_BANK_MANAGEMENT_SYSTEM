import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;

public class UserDashboard extends JFrame {
    private Image bgImage;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private String userName;

    public UserDashboard(int userId, String userName) {
        this.userName = userName;
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - User Portal");
        setSize(1100, 800); // Slightly taller to fit two tables comfortably
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel bgPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g.setColor(new Color(255, 240, 240, 150)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bgPanel);

        // --- SIDEBAR ---
        JPanel sidebar = createSidebar();
        bgPanel.add(sidebar);

        // --- CONTENT PANEL ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBounds(300, 50, 750, 680);
        contentPanel.setOpaque(false);
        bgPanel.add(contentPanel);

        // --- VIEWS ---
        contentPanel.add(createProfileView(), "My Profile");
        contentPanel.add(createCampsView(), "Blood Camps");
        contentPanel.add(createRequestView(), "Request Blood");
        contentPanel.add(createContactView(), "Contact Staff");

        cardLayout.show(contentPanel, "My Profile");
    }

    private JPanel createSidebar() {
        JPanel sb = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 40, 40, 230)); 
                g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 30, 30);
                g2.dispose();
            }
        };
        sb.setBounds(0, 0, 260, 800);
        sb.setOpaque(false);

        JLabel title = new JLabel("USER PORTAL", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(20, 60, 220, 30);
        sb.add(title);

        String[] items = {"My Profile", "Blood Camps", "Request Blood", "Contact Staff", "Logout"};
        int y = 150;
        for (String item : items) {
            JButton btn = createPillButton(item);
            btn.setBounds(40, y, 180, 45);
            btn.addActionListener(e -> {
                if (item.equals("Logout")) {
                    dispose();
                    new UserLoginFrame("User").setVisible(true);
                } else {
                    cardLayout.show(contentPanel, item);
                    refreshAllData();
                }
            });
            sb.add(btn);
            y += 65;
        }
        return sb;
    }

    private JButton createPillButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 50));
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 45, 45); // Fully pill-shaped
                g2.setColor(new Color(255, 255, 255, 120));
                g2.drawRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 45, 45);
                super.paint(g2, c);
                g2.dispose();
            }
        });
        return btn;
    }

    // --- SEPARATED PROFILE VIEW ---
    private DefaultTableModel donationModel, requestModel;

    private JPanel createProfileView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        JLabel welcome = new JLabel("Hello, " + userName.toUpperCase());
        welcome.setFont(new Font("Arial", Font.BOLD, 30));
        welcome.setForeground(new Color(120, 0, 0));
        welcome.setBounds(20, 0, 500, 40);
        panel.add(welcome);

        // 1. Donation History Section
        JLabel donationLabel = new JLabel("My Donation History");
        donationLabel.setFont(new Font("Arial", Font.BOLD, 16));
        donationLabel.setBounds(20, 50, 200, 30);
        panel.add(donationLabel);

        String[] donCols = {"Date", "Location", "Units", "Status"};
        donationModel = new DefaultTableModel(donCols, 0);
        JTable donTable = styleTable(new JTable(donationModel));
        panel.add(createTableScroll(donTable, 20, 85, 700, 200));

        // 2. Blood Requests Section
        JLabel requestLabel = new JLabel("My Blood Requests");
        requestLabel.setFont(new Font("Arial", Font.BOLD, 16));
        requestLabel.setBounds(20, 310, 200, 30);
        panel.add(requestLabel);

        String[] reqCols = {"ID", "Blood Group", "Units", "Status", "Date"};
        requestModel = new DefaultTableModel(reqCols, 0);
        JTable reqTable = styleTable(new JTable(requestModel));
        panel.add(createTableScroll(reqTable, 20, 345, 700, 200));

        loadProfileData();
        return panel;
    }

    private void loadProfileData() {
        try (Connection conn = DbConnection.connect()) {
            // Fetch Requests - Using column names from image_4f5478.png and image_efde9d.png
            String reqSql = "SELECT request_id, blood_group, units_required, status, request_date FROM blood_requests WHERE requester_name = ?";
            PreparedStatement psReq = conn.prepareStatement(reqSql);
            psReq.setString(1, userName);
            ResultSet rsReq = psReq.executeQuery();
            requestModel.setRowCount(0);
            while (rsReq.next()) {
                requestModel.addRow(new Object[]{rsReq.getInt(1), rsReq.getString(2), rsReq.getInt(3), rsReq.getString(4), rsReq.getDate(5)});
            }

            // Fetch Donations - Pulling from donors table based on donor name
            String donSql = "SELECT last_donation_date, address, 'N/A', 'Completed' FROM donors WHERE name = ?";
            PreparedStatement psDon = conn.prepareStatement(donSql);
            psDon.setString(1, userName);
            ResultSet rsDon = psDon.executeQuery();
            donationModel.setRowCount(0);
            while (rsDon.next()) {
                donationModel.addRow(new Object[]{rsDon.getDate(1), rsDon.getString(2), "1 Unit", rsDon.getString(4)});
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- OTHER FUNCTIONALITIES ---
    private JPanel createCampsView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        JLabel title = new JLabel("Available Blood Camps");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setBounds(20, 10, 400, 30);
        panel.add(title);

        String[] cols = {"Camp Name", "Location", "Date", "Time", "Organizer"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));
        panel.add(createTableScroll(table, 20, 60, 700, 500));

        try (Connection conn = DbConnection.connect()) {
            // Query based on image_4f5478.png
            ResultSet rs = conn.createStatement().executeQuery("SELECT camp_name, location, camp_date, camp_time, organizer FROM blood_camps");
            while (rs.next()) model.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getDate(3), rs.getString(4), rs.getString(5)});
        } catch (Exception e) { e.printStackTrace(); }
        return panel;
    }

    // --- VIEW 3: REQUEST BLOOD FORM ---
    private JPanel createRequestView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        JLabel title = new JLabel("Submit New Blood Request");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0)); // Title color matching rest of dashboard
        title.setBounds(20, 10, 500, 40);
        panel.add(title);

        // --- ROUNDED FORM CONTAINER (REUSING FROM "ADD CAMP") ---
        JPanel form = createRoundedFormContainer(20, 70, 500, 300);
        form.setLayout(new GridLayout(4, 1, 10, 10)); // Simple GridLayout for fields
        panel.add(form);

        // Input Fields (with placeholders matching your design)
        JComboBox<String> comboGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        JTextField txtUnits = new JTextField("");
        JTextField txtContact = new JTextField("");

        // Setting placeholders or default text
        txtUnits.setForeground(Color.GRAY);
        txtContact.setForeground(Color.GRAY);

        // Form Fields (Label and Input)
        form.add(new JLabel("Blood Group:")); 
        form.add(comboGroup);
        form.add(new JLabel("Units Required:")); 
        form.add(txtUnits);
        form.add(new JLabel("Contact Number:")); 
        form.add(txtContact); // REPLACED: Reason with Contact Number

        // --- SUBMIT BUTTON WITH ROUNDED CORNERS ---
        JButton btnSubmit = new JButton("Submit Request") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fill vibrant red background with rounded corners
                g2.setColor(new Color(180, 0, 0)); // Vibrant Red matching header
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.dispose();
                super.paintComponent(g); // Draw text on top
            }
        };

        btnSubmit.setBounds(20, 390, 200, 45); // Positioning below the form
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorderPainted(false); // Clean rounded button look
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 16));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnSubmit.addActionListener(e -> {
            String bGroup = comboGroup.getSelectedItem().toString();
            String units = txtUnits.getText();
            String contact = txtContact.getText();

            // Input Validation
            if (units.isEmpty() || contact.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Missing Info", JOptionPane.WARNING_MESSAGE);
                return;
            }

            submitRequestToDB(bGroup, units, contact);
        });

        panel.add(btnSubmit);
        return panel;
    }

    private void submitRequestToDB(String group, String units, String contact) {
        // Use exact column names from your database schema (image_0cf9a2.png)
        try (Connection conn = DbConnection.connect()) {
            String sql = "INSERT INTO blood_requests (requester_name, blood_group, units_required, requester_type, contact_number, status, request_date) VALUES (?, ?, ?, 'User', ?, 'Pending', CURDATE())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, userName); // Matches requester_name
            ps.setString(2, group);
            ps.setInt(3, Integer.parseInt(units));
            ps.setString(4, contact); // Sets the contact_number
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Request Submitted Successfully!");
            
            // Navigate back to profile and refresh history
            cardLayout.show(contentPanel, "My Profile");
            refreshAllData(); // Method to reload your tables
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error: Units must be a number!");
        }
    }
   private JPanel createContactView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);

        JLabel title = new JLabel("Contact Admin Staff");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(new Color(150, 0, 0));
        title.setBounds(20, 10, 400, 40);
        panel.add(title);

        // Updated columns: Removed Email, kept only Name and Phone
        String[] cols = {"Staff Name", "Contact Number"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));
        
        panel.add(createTableScroll(table, 20, 70, 700, 400));

        // Load only the necessary columns from the database
        try (Connection conn = DbConnection.connect()) {
            // Selecting only name and contact info
            String sql = "SELECT name, phone FROM users WHERE role = 'Admin'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) { // Fixed the "Before start of result set" error
                model.addRow(new Object[]{
                    rs.getString("name"), 
                    rs.getString("phone")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return panel;
    }

    // --- HELPERS ---
    private JTable styleTable(JTable table) {
        table.setRowHeight(30);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(180, 0, 0));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Arial", Font.BOLD, 14));
        return table;
    }

    private JScrollPane createTableScroll(JTable table, int x, int y, int w, int h) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBounds(x, y, w, h);
        sp.setBorder(BorderFactory.createLineBorder(new Color(180, 0, 0), 1));
        return sp;
    }
private JPanel createRoundedFormContainer(int x, int y, int w, int h) {
    JPanel container = new JPanel(null) {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // White background
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            
            // Red Border (Matches your "Add Camp" style)
            g2.setColor(new Color(160, 20, 20)); 
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 40, 40);
            g2.dispose();
        }
    };
    container.setOpaque(false);
    container.setBounds(x, y, w, h);
    container.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    return container;
}
    private void refreshAllData() { loadProfileData(); }
}
