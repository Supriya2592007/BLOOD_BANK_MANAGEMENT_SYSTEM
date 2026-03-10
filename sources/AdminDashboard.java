import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private Image bgImage;
    private JLabel lblTotalUnits;
    private JLabel lblTotalreqs;
    private JLabel lblTotalcamps;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public AdminDashboard() {
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - Administrator Command Center");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // --- MAIN BACKGROUND PANEL ---
        JPanel bgPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.setColor(new Color(255, 230, 230, 130)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bgPanel);

        // --- SIDEBAR (Deep Red) ---
        JPanel sidebar = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(160, 20, 20, 210)); 
                g2.fillRoundRect(20, 20, getWidth() - 40, getHeight() - 40, 30, 30);
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setBounds(0, 0, 260, 750);
        bgPanel.add(sidebar);

        JLabel sideTitle = new JLabel("ADMIN PANEL", SwingConstants.CENTER);
        sideTitle.setBounds(20, 60, 220, 30);
        sideTitle.setForeground(Color.WHITE);
        sideTitle.setFont(new Font("Arial", Font.BOLD, 22));
        sidebar.add(sideTitle);

        // --- CONTENT PANEL ---
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBounds(300, 50, 750, 630);
        contentPanel.setOpaque(false);
        bgPanel.add(contentPanel);

        // Adding Views
        contentPanel.add(createDashboardView(), "Dashboard");
        contentPanel.add(createInventoryView(), "Inventory");
        contentPanel.add(createRequestsView(), "Blood Requests");
        contentPanel.add(createCampsView(), "Blood Camps");
        contentPanel.add(createAddCampView(), "Add Blood Camp");
        // --- NAVIGATION BUTTONS ---
        // Inside AdminDashboard constructor
     String[] menuItems = {"Dashboard", "Inventory", "Blood Requests", "Blood Camps", "Add Blood Camp", "Logout"};
        int btnY = 150;
        for (String item : menuItems) {
            LightPinkButton btn = new LightPinkButton(item);
            btn.setBounds(40, btnY, 180, 45);
            sidebar.add(btn);
            btnY += 60;

            btn.addActionListener(e -> {
                String cmd = e.getActionCommand();
                if (cmd.equals("Logout")) {
                    this.dispose();
                } else {
                    cardLayout.show(contentPanel, cmd);
                    if (cmd.equals("Dashboard")) fetchLiveStock();
                }
            });
        }
        
        fetchLiveStock();
    }

    private JPanel createDashboardView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        JLabel welcome = new JLabel("Welcome, Administrator");
        welcome.setBounds(0, 0, 500, 40);
        welcome.setFont(new Font("Arial", Font.BOLD, 36));
        welcome.setForeground(new Color(120, 0, 0));
        panel.add(welcome);

        lblTotalUnits = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Total Units in Stock", lblTotalUnits, 0, 80, Color.BLUE));
         lblTotalreqs = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Active Requests", lblTotalreqs, 250, 80, Color.ORANGE));
         lblTotalcamps = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Upcoming Camps", lblTotalcamps, 500, 80, new Color(0, 150, 0)));
        return panel;
    }

    private JPanel createInventoryView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        JLabel title = new JLabel("Current Blood Stock Inventory");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        JPanel tableContainer = createRoundedTableContainer(20, 70, 700, 320);
        mainPanel.add(tableContainer);

        String[] cols = {"Blood Group", "Total Units"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        // Logic to load inventory data
        Runnable loadStock = () -> {
            model.setRowCount(0);
            try (Connection conn = DbConnection.connect()) {
                String sql = "SELECT blood_group, units_available FROM blood_stock"; 
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getString(1), rs.getInt(2)});
                }
            } catch (SQLException e) { System.out.println(e.getMessage()); }
        };

        loadStock.run();

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        // REFRESH BUTTON ADDED
        JButton btnRefresh = new JButton("Refresh Stock");
        btnRefresh.setBounds(20, 400, 150, 40);
        btnRefresh.setBackground(new Color(160, 20, 20)); // Matching Sidebar Red
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadStock.run();
            fetchLiveStock(); // Also update dashboard card
            act_req();
            act_camps();
            JOptionPane.showMessageDialog(this, "Inventory Updated!");
        });
        mainPanel.add(btnRefresh);

        return mainPanel;
    }

    private JPanel createRequestsView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);

        JLabel title = new JLabel("Pending Blood Requests");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        JPanel tableContainer = createRoundedTableContainer(20, 70, 700, 420);
        mainPanel.add(tableContainer);

        String[] cols = {"ID", "Requester", "Group", "Units Req.", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        try (Connection conn = DbConnection.connect()) {
            String sql = "SELECT request_id, requester_name, blood_group, units_required, status FROM blood_requests WHERE status='Pending'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) {
                model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5)});
            }
        } catch (SQLException e) { System.out.println(e.getMessage()); }

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnApprove = new JButton("Approve Selection");
        btnApprove.setBounds(20, 510, 200, 45);
        btnApprove.setBackground(new Color(210, 40, 40));
        btnApprove.setForeground(Color.WHITE);
        btnApprove.setFont(new Font("Arial", Font.BOLD, 14));
        btnApprove.addActionListener(e -> handleApproval(table, model));
        mainPanel.add(btnApprove);

        return mainPanel;
    }
// --- VIEW 4: BLOOD CAMPS ---
    private JPanel createCampsView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);

        // Title styling
        JLabel title = new JLabel("Upcoming Blood Donation Camps");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        // Rounded Table Container
        JPanel tableContainer = createRoundedTableContainer(20, 70, 700, 320);
        mainPanel.add(tableContainer);

        // Table Setup
        String[] cols = {"Camp ID", "Camp Name", "Location", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        // Logic to load Camp data
        Runnable loadCamps = () -> {
            model.setRowCount(0);
            try (Connection conn = DbConnection.connect()) {
                // Adjust table/column names if they differ in your DB
                String sql = "SELECT camp_id, camp_name, location, camp_date FROM blood_camps"; 
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt(1), 
                        rs.getString(2), 
                        rs.getString(3), 
                        rs.getString(4)
                    });
                }
            } catch (SQLException e) { 
                System.out.println("Camp Load Error: " + e.getMessage()); 
            }
        };

        loadCamps.run();

        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        // REFRESH BUTTON (Matching Inventory style)
        JButton btnRefresh = new JButton("Refresh Camps");
        btnRefresh.setBounds(20, 400, 150, 40);
        btnRefresh.setBackground(new Color(160, 20, 20)); // Sidebar Red
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFocusPainted(false);
        btnRefresh.addActionListener(e -> {
            loadCamps.run();
            JOptionPane.showMessageDialog(this, "Camp Schedule Updated!");
        });
        mainPanel.add(btnRefresh);

        return mainPanel;
    }
    
    // --- VIEW 5: ADD BLOOD CAMP ---
    private JPanel createAddCampView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);

        JLabel title = new JLabel("Schedule New Blood Camp");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        // --- ROUNDED FORM PANEL WITH RED BORDER ---
        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                
                // ADDED: Red border around the container (matches Card accent color)
                g2.setColor(new Color(160, 20, 20)); // Deep Red
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
                
                g2.dispose();
            }
        };
        formPanel.setOpaque(false);
        // Changed height/positioning for a tighter fit
        formPanel.setBounds(20, 70, 500, 380); 
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(formPanel);

        // Input Fields
        JTextField txtName = new JTextField();
        JTextField txtLoc = new JTextField();
        JTextField txtDate = new JTextField("YYYY-MM-DD");
        JTextField txtTime = new JTextField("10:00 AM");
        JTextField txtOrg = new JTextField();

        formPanel.add(new JLabel("Camp Name:")); formPanel.add(txtName);
        formPanel.add(new JLabel("Location:")); formPanel.add(txtLoc);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):")); formPanel.add(txtDate);
        formPanel.add(new JLabel("Time:")); formPanel.add(txtTime);
        formPanel.add(new JLabel("Organizer:")); formPanel.add(txtOrg);

        // --- CUSTOM ROUNDED RED BUTTON (Similar to LightPinkButton structure) ---
        JButton btnSubmit = new JButton("Register Camp") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Vibrant red background
                g2.setColor(new Color(210, 40, 40)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        
        btnSubmit.setBounds(20, 470, 200, 45); // Positioned below the container
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorderPainted(false);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));
        btnSubmit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSubmit.addActionListener(e -> {
            String date = txtDate.getText();
            try (Connection conn = DbConnection.connect()) {
                // Conflict Checking: Ensure no other camp is on the same date
                String checkSql = "SELECT COUNT(*) FROM blood_camps WHERE camp_date = ?";
                PreparedStatement psCheck = conn.prepareStatement(checkSql);
                psCheck.setString(1, date);
                ResultSet rs = psCheck.executeQuery();
                rs.next();
                
                if (rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "A camp is already scheduled for " + date + "!", "Schedule Conflict", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Insertion Logic
                String sql = "INSERT INTO blood_camps (camp_name, location, camp_date, camp_time, organizer) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, txtLoc.getText());
                ps.setString(3, date);
                ps.setString(4, txtTime.getText());
                ps.setString(5, txtOrg.getText());

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Camp added successfully!");
                txtName.setText(""); txtLoc.setText(""); txtDate.setText("YYYY-MM-DD");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
            }
        });

        mainPanel.add(btnSubmit);
        return mainPanel;
    }
    private JPanel createRoundedTableContainer(int x, int y, int w, int h) {
        JPanel container = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        container.setOpaque(false);
        container.setBounds(x, y, w, h);
        container.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        return container;
    }

    private JTable styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 15));
        table.getTableHeader().setBackground(new Color(160, 20, 20));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        table.getTableHeader().setPreferredSize(new Dimension(100, 35));
        return table;
    }

    private void handleApproval(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a request!");
            return;
        }
        int reqId = (int) model.getValueAt(row, 0);
        String bGroup = (String) model.getValueAt(row, 2);
        int unitsRequired = (int) model.getValueAt(row, 3);

        try (Connection conn = DbConnection.connect()) {
            // CHECKING: IF (units_available >= units_required)
            String checkSql = "SELECT units_available FROM blood_stock WHERE blood_group = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, bGroup);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next()) {
                int available = rs.getInt("units_available");
                if (available >= unitsRequired) {
                    conn.setAutoCommit(false);
                    // 1. Update Inventory
                    PreparedStatement ps1 = conn.prepareStatement("UPDATE blood_stock SET units_available = units_available - ? WHERE blood_group = ?");
                    ps1.setInt(1, unitsRequired);
                    ps1.setString(2, bGroup);
                    ps1.executeUpdate();

                    // 2. Update Request
                    PreparedStatement ps2 = conn.prepareStatement("UPDATE blood_requests SET status = 'Approved' WHERE request_id = ?");
                    ps2.setInt(1, reqId);
                    ps2.executeUpdate();

                    conn.commit();
                    model.removeRow(row);
                    fetchLiveStock();
                    JOptionPane.showMessageDialog(this, "Success: Request Approved & Stock Updated!");
                } else {
                    // ELSE: Insufficient Blood
                    JOptionPane.showMessageDialog(this, "Error: Insufficient Blood in Stock!", "Insufficient Blood", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    // private JPanel createPlaceholderView(String name) {
    //     JPanel p = new JPanel(new GridBagLayout());
    //     p.setOpaque(false);
    //     p.add(new JLabel(name + " Module Coming Soon..."));
    //     return p;
    // }

    private class LightPinkButton extends JButton {
        private boolean hovered = false;
        public LightPinkButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setForeground(Color.WHITE);
            setFont(new Font("Arial", Font.BOLD, 14));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, hovered ? 80 : 40)); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private JPanel createCard(String title, JLabel valLabel, int x, int y, Color accent) {
        JPanel card = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                g2.dispose();
            }
        };
        card.setBounds(x, y, 220, 150);
        card.setOpaque(false);
        JLabel t = new JLabel(title, SwingConstants.CENTER);
        t.setBounds(0, 20, 220, 20);
        t.setFont(new Font("Arial", Font.BOLD, 15));
        card.add(t);
        valLabel.setBounds(0, 60, 220, 50);
        valLabel.setFont(new Font("Arial", Font.BOLD, 44));
        valLabel.setForeground(accent);
        card.add(valLabel);
        return card;
    }

    private void fetchLiveStock() {
        try (Connection conn = DbConnection.connect()) {
            if (conn != null) {
                String query = "SELECT SUM(units_available) FROM blood_stock";
                ResultSet rs = conn.createStatement().executeQuery(query);
               if (rs.next()) lblTotalUnits.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { lblTotalUnits.setText("0"); }
    }
    private void act_req() {
        try (Connection conn = DbConnection.connect()) {
            if (conn != null) {
                String query = "SELECT COUNT(*) FROM blood_requests WHERE status='Pending'";
                ResultSet rs = conn.createStatement().executeQuery(query);
                if (rs.next()) lblTotalUnits.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { lblTotalreqs.setText("0"); }
    }
    private void act_camps() {
        try (Connection conn = DbConnection.connect()) {
            if (conn != null) {
                String query = "SELECT COUNT(*) FROM blood_camps";
                ResultSet rs = conn.createStatement().executeQuery(query);
                if (rs.next()) lblTotalUnits.setText(String.valueOf(rs.getInt(1)));
            }
        } catch (SQLException e) { lblTotalcamps.setText("0"); }
    }
}
