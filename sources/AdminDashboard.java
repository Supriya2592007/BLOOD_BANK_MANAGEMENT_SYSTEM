import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class AdminDashboard extends JFrame {
    private Image bgImage;
    private JLabel lblTotalUnits, lblTotalreqs, lblTotalcamps;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    public AdminDashboard() {
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - Administrator Command Center");
        setSize(1150, 750);
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

        // --- SIDEBAR ---
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
        contentPanel.setBounds(300, 50, 800, 630);
        contentPanel.setOpaque(false);
        bgPanel.add(contentPanel);

        // Adding All Views
        contentPanel.add(createDashboardView(), "Dashboard");
        contentPanel.add(createInventoryView(), "Inventory");
        contentPanel.add(createRequestsView(), "User Requests");
        contentPanel.add(createHospitalRequestsView(), "Manage Hospitals");
        contentPanel.add(createAddDonorView(), "Add Donor");
         contentPanel.add(createDonorView(), "View Donors");
        contentPanel.add(createCampsView(), "Blood Camps");
        contentPanel.add(createAddCampView(), "Add Blood Camp");

        // --- NAVIGATION ---
        String[] menuItems = {"Dashboard", "Inventory", "User Requests", "Manage Hospitals", "Add Donor", "Blood Camps", "Add Blood Camp", "Logout"};
        int btnY = 110;
        for (String item : menuItems) {
            LightPinkButton btn = new LightPinkButton(item);
            btn.setBounds(40, btnY, 180, 40);
            sidebar.add(btn);
            btnY += 50;

            btn.addActionListener(e -> {
                String cmd = e.getActionCommand();
                if (cmd.equals("Logout")) {
                    this.dispose();
                    new MainWindow().setVisible(true);;
                } else {
                    cardLayout.show(contentPanel, cmd);
                    updateAllLiveCounts();
                }
            });
        }
        updateAllLiveCounts();
    }

    private JPanel createDashboardView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        JLabel welcome = new JLabel("Welcome, Administrator");
        welcome.setBounds(0, 0, 500, 45);
        welcome.setFont(new Font("Arial", Font.BOLD, 36));
        welcome.setForeground(new Color(120, 0, 0));
        panel.add(welcome);

        lblTotalUnits = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Total Units in Stock", lblTotalUnits, 0, 80, Color.BLUE));
        lblTotalreqs = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Active Requests", lblTotalreqs, 260, 80, Color.ORANGE));
        lblTotalcamps = new JLabel("0", SwingConstants.CENTER);
        panel.add(createCard("Upcoming Camps", lblTotalcamps, 520, 80, new Color(0, 150, 0)));
        return panel;
    }

    // --- UPDATED ADD DONOR MODULE (SYNCED WITH USERS TABLE) ---
    private JPanel createAddDonorView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        JLabel title = new JLabel("Record Donation & Register Donor");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBounds(20, 10, 600, 40);
        title.setForeground(new Color(120, 0, 0));
        panel.add(title);

        JPanel form = createRoundedTableContainer(20, 70, 650, 480);
        form.setLayout(new GridLayout(9, 2, 10, 15));
        
        JTextField tName = new JTextField();
        JTextField tAge = new JTextField();
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        JComboBox<String> cbGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        JTextField tPhone = new JTextField();
        JTextField tEmail = new JTextField();
        JTextField tAddress = new JTextField();
        JTextField tLastDonation = new JTextField("YYYY-MM-DD");
        JTextField tUnits = new JTextField();

        form.add(new JLabel("Full Name:")); form.add(tName);
        form.add(new JLabel("Age:")); form.add(tAge);
        form.add(new JLabel("Gender:")); form.add(cbGender);
        form.add(new JLabel("Blood Group:")); form.add(cbGroup);
        form.add(new JLabel("Phone (Password):")); form.add(tPhone);
        form.add(new JLabel("Email:")); form.add(tEmail);
        form.add(new JLabel("Address:")); form.add(tAddress);
        form.add(new JLabel("Last Donation:")); form.add(tLastDonation);
        form.add(new JLabel("Units Donated (ml):")); form.add(tUnits);

        JButton btnSave = new JButton("Register Everything");
        btnSave.setBounds(20, 560, 250, 45);
        btnSave.setBackground(new Color(120, 0, 0));
        btnSave.setForeground(Color.WHITE);

        btnSave.addActionListener(e -> {
            try (Connection conn = DbConnection.connect()) {
                conn.setAutoCommit(false);

                // 1. Insert into 'donors' table
                String donorQuery = "INSERT INTO donors (name, age, gender, blood_group, phone, email, address, last_donation_date) VALUES (?,?,?,?,?,?,?,?)";
                PreparedStatement ps1 = conn.prepareStatement(donorQuery);
                ps1.setString(1, tName.getText());
                ps1.setInt(2, Integer.parseInt(tAge.getText()));
                ps1.setString(3, cbGender.getSelectedItem().toString());
                ps1.setString(4, cbGroup.getSelectedItem().toString());
                ps1.setString(5, tPhone.getText());
                ps1.setString(6, tEmail.getText());
                ps1.setString(7, tAddress.getText());
                String dateVal = tLastDonation.getText().equals("YYYY-MM-DD") ? null : tLastDonation.getText();
                ps1.setString(8, dateVal);
                ps1.executeUpdate();

                // 2. Update Inventory
                PreparedStatement ps2 = conn.prepareStatement("UPDATE blood_stock SET units_available = units_available + ? WHERE blood_group = ?");
                ps2.setInt(1, Integer.parseInt(tUnits.getText()));
                ps2.setString(2, cbGroup.getSelectedItem().toString());
                ps2.executeUpdate();

                // 3. Sync to 'users' table
                PreparedStatement ps3 = conn.prepareStatement("INSERT INTO users (username, password, name, phone, blood_group, role) VALUES (?,?,?,?,?,?)");
                ps3.setString(1, tName.getText());   // Username = Name
                ps3.setString(2, tPhone.getText());  // Password = Phone
                ps3.setString(3, tName.getText());
                ps3.setString(4, tPhone.getText());
                ps3.setString(5, cbGroup.getSelectedItem().toString());
                ps3.setString(6, "USER");  // Default role
                ps3.executeUpdate();

                conn.commit();
                JOptionPane.showMessageDialog(this, "Success! Donor and User account created.\nLogin with Name and Phone.");
                updateAllLiveCounts();
                
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); 
                ex.printStackTrace();
            }
        });

        panel.add(form); 
        panel.add(btnSave);
        return panel;
    }

    private JPanel createHospitalRequestsView() {
        JPanel panel = new JPanel(null);
        panel.setOpaque(false);
        JLabel title = new JLabel("Hospital Orders");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBounds(20, 10, 400, 40);
        panel.add(title);

        JPanel container = createRoundedTableContainer(20, 70, 750, 400);
        String[] cols = {"ID", "Hospital", "Group", "Units", "Status"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        try (Connection conn = DbConnection.connect()) {
            String sql = "SELECT r.request_id, h.hospital_name, r.blood_group, r.units, r.status FROM Hospital_blood_requests r JOIN hospitals h ON r.hospital_id = h.hospital_id WHERE r.status='Pending'";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while(rs.next()) model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5)});
        } catch (SQLException e) { e.printStackTrace(); }

        container.add(new JScrollPane(table));
        panel.add(container);

        JButton btnApp = new JButton("Approve Hospital Request");
        btnApp.setBounds(20, 490, 220, 45);
        btnApp.setBackground(new Color(160, 20, 20));
        btnApp.setForeground(Color.WHITE);
        btnApp.addActionListener(e -> handleHospitalApproval(table, model));
        panel.add(btnApp);

         JButton btnreject = new JButton("Reject Selection");
        btnreject.setBounds(290, 490, 220, 45);
        btnreject.setBackground(new Color(210, 40, 40));
        btnreject.setForeground(Color.WHITE);
        btnreject.addActionListener(e -> RejectApproval(table, model,"Hospital"));
        panel.add(btnreject);

        return panel;
    }

    private void handleHospitalApproval(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        String grp = (String) model.getValueAt(row, 2);
        int units = (int) model.getValueAt(row, 3);

        try (Connection conn = DbConnection.connect()) {
            PreparedStatement ps = conn.prepareStatement("SELECT units_available FROM blood_stock WHERE blood_group = ?");
            ps.setString(1, grp);
            ResultSet rs = ps.executeQuery();
            if (rs.next() && rs.getInt(1) >= units) {
                conn.setAutoCommit(false);
                conn.createStatement().executeUpdate("UPDATE blood_stock SET units_available = units_available - "+units+" WHERE blood_group = '"+grp+"'");
                conn.createStatement().executeUpdate("UPDATE Hospital_blood_requests SET status = 'Approved' WHERE request_id = "+id);
                conn.commit();
                model.removeRow(row);
                updateAllLiveCounts();
                JOptionPane.showMessageDialog(this, "Approved!");
            } else { JOptionPane.showMessageDialog(this, "Insufficient Stock!"); }
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void updateAllLiveCounts() {
        fetchLiveStock();
        act_req();
        act_camps();
    }

    private void fetchLiveStock() {
        try (Connection conn = DbConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT SUM(units_available) FROM blood_stock");
            if (rs.next()) lblTotalUnits.setText(String.valueOf(rs.getInt(1)));
        } catch (Exception e) { lblTotalUnits.setText("0"); }
    }

    private void act_req() {
        try (Connection conn = DbConnection.connect()) {
            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM blood_requests WHERE status='Pending'");
            int c1 = rs1.next() ? rs1.getInt(1) : 0;
            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM Hospital_blood_requests WHERE status='Pending'");
            int c2 = rs2.next() ? rs2.getInt(1) : 0;
            lblTotalreqs.setText(String.valueOf(c1 + c2));
        } catch (Exception e) { lblTotalreqs.setText("0"); }
    }

    private void act_camps() {
        try (Connection conn = DbConnection.connect()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) FROM blood_camps");
            if (rs.next()) lblTotalcamps.setText(String.valueOf(rs.getInt(1)));
        } catch (Exception e) { lblTotalcamps.setText("0"); }
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

        JButton btnRefresh = new JButton("Refresh Stock");
        btnRefresh.setBounds(20, 400, 150, 40);
        btnRefresh.setBackground(new Color(160, 20, 20));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> {
            loadStock.run();
            updateAllLiveCounts();
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
        btnApprove.addActionListener(e -> handleApproval(table, model));
        mainPanel.add(btnApprove);

         JButton btnreject = new JButton("Reject Selection");
        btnreject.setBounds(270, 510, 200, 45);
        btnreject.setBackground(new Color(210, 40, 40));
        btnreject.setForeground(Color.WHITE);
        btnreject.addActionListener(e -> RejectApproval(table, model,"User"));
        mainPanel.add(btnreject);

        return mainPanel;
    }

    private JPanel createCampsView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        JLabel title = new JLabel("Upcoming Blood Donation Camps");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        JPanel tableContainer = createRoundedTableContainer(20, 70, 700, 320);
        mainPanel.add(tableContainer);

        String[] cols = {"Camp ID", "Camp Name", "Location", "Date"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        Runnable loadCamps = () -> {
            model.setRowCount(0);
            try (Connection conn = DbConnection.connect()) {
                String sql = "SELECT camp_id, camp_name, location, camp_date FROM blood_camps"; 
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)});
                }
            } catch (SQLException e) { System.out.println(e.getMessage()); }
        };

        loadCamps.run();
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Camps");
        btnRefresh.setBounds(20, 400, 150, 40);
        btnRefresh.setBackground(new Color(160, 20, 20));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> { loadCamps.run(); });
        mainPanel.add(btnRefresh);

        return mainPanel;
    }
    private JPanel createDonorView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        JLabel title = new JLabel("Donors");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        JPanel tableContainer = createRoundedTableContainer(20, 70, 700, 320);
        mainPanel.add(tableContainer);

        String[] cols = {"Donor ID", "Donor Name", "Blood Group", "Contact no","Email"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        JTable table = styleTable(new JTable(model));

        Runnable loadCamps = () -> {
            model.setRowCount(0);
            try (Connection conn = DbConnection.connect()) {
                String sql = "SELECT donor_id, name, blood_group, phone,email FROM donors"; 
                ResultSet rs = conn.createStatement().executeQuery(sql);
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4),rs.getString(5)});
                }
            } catch (SQLException e) { System.out.println(e.getMessage()); }
        };

        loadCamps.run();
        tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Donors");
        btnRefresh.setBounds(20, 400, 150, 40);
        btnRefresh.setBackground(new Color(160, 20, 20));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.addActionListener(e -> { loadCamps.run(); });
        mainPanel.add(btnRefresh);

        return mainPanel;
    }
    
    private JPanel createAddCampView() {
        JPanel mainPanel = new JPanel(null);
        mainPanel.setOpaque(false);
        JLabel title = new JLabel("Schedule New Blood Camp");
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setForeground(new Color(120, 0, 0));
        title.setBounds(20, 10, 500, 40);
        mainPanel.add(title);

        JPanel formPanel = new JPanel(new GridLayout(6, 1, 10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(160, 20, 20)); 
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 40, 40);
                g2.dispose();
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBounds(20, 70, 500, 380); 
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.add(formPanel);

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

        JButton btnSubmit = new JButton("Register Camp");
        btnSubmit.setBounds(20, 470, 200, 45); 
        btnSubmit.setBackground(new Color(210, 40, 40));
        btnSubmit.setForeground(Color.WHITE);
        
        btnSubmit.addActionListener(e -> {
            try (Connection conn = DbConnection.connect()) {
                String sql = "INSERT INTO blood_camps (camp_name, location, camp_date, camp_time, organizer) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setString(2, txtLoc.getText());
                ps.setString(3, txtDate.getText());
                ps.setString(4, txtTime.getText());
                ps.setString(5, txtOrg.getText());
                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Camp added successfully!");
                updateAllLiveCounts();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
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
        table.getTableHeader().setBackground(new Color(160, 20, 20));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        return table;
    }

    private void handleApproval(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int reqId = (int) model.getValueAt(row, 0);
        String bGroup = (String) model.getValueAt(row, 2);
        int unitsRequired = (int) model.getValueAt(row, 3);

        try (Connection conn = DbConnection.connect()) {
            String checkSql = "SELECT units_available FROM blood_stock WHERE blood_group = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, bGroup);
            ResultSet rs = psCheck.executeQuery();

            if (rs.next() && rs.getInt("units_available") >= unitsRequired) {
                conn.setAutoCommit(false);
                PreparedStatement ps1 = conn.prepareStatement("UPDATE blood_stock SET units_available = units_available - ? WHERE blood_group = ?");
                ps1.setInt(1, unitsRequired);
                ps1.setString(2, bGroup);
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement("UPDATE blood_requests SET status = 'Approved' WHERE request_id = ?");
                ps2.setInt(1, reqId);
                ps2.executeUpdate();

                conn.commit();
                model.removeRow(row);
                updateAllLiveCounts();
                JOptionPane.showMessageDialog(this, "Approved!");
            } else {
                JOptionPane.showMessageDialog(this, "Insufficient Blood Stock!");
            }
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }
 private void RejectApproval(JTable table, DefaultTableModel model,String role) {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int reqId = (int) model.getValueAt(row, 0);
        String bGroup = (String) model.getValueAt(row, 2);
       // int unitsRequired = (int) model.getValueAt(row, 3);

        try (Connection conn = DbConnection.connect()) {
            String checkSql = "SELECT units_available FROM blood_stock WHERE blood_group = ?";
            PreparedStatement psCheck = conn.prepareStatement(checkSql);
            psCheck.setString(1, bGroup);
            ResultSet rs = psCheck.executeQuery();
                 conn.setAutoCommit(false); 
                 if(role.equals("User")){
             PreparedStatement ps2 = conn.prepareStatement("UPDATE blood_requests SET status = 'REJECTED' WHERE request_id = ?");
                ps2.setInt(1, reqId);
                ps2.executeUpdate();
                 }
                 else if(role.equals("Hospital")){
                     PreparedStatement ps2 = conn.prepareStatement("UPDATE Hospital_blood_requests SET status = 'REJECTED' WHERE request_id = ?");
                    ps2.setInt(1, reqId);
                    ps2.executeUpdate();
                 }

                conn.commit();
                model.removeRow(row);
                updateAllLiveCounts();
                JOptionPane.showMessageDialog(this, "Request Rejected!");
        } catch (SQLException ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

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
}
