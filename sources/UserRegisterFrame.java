import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class UserRegisterFrame extends JFrame {
    private Image bgImage;
    private JTextField txtName, txtAge, txtPhone, txtAddress, txtUser;
    private JPasswordField txtPass;
    private JComboBox<String> genderBox, bloodBox;
    private String registrationRole; // Stores the role passed from the login context

    public UserRegisterFrame(String role) {
        this.registrationRole = role;
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - Create New " + registrationRole + " Account");
        setSize(1000, 750); 
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel bgPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                g2d.setColor(new Color(255, 230, 230, 120)); 
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bgPanel);

        JPanel regPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 35, 35);
                g2.setColor(new Color(180, 0, 0)); 
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 35, 35);
                g2.dispose();
            }
        };
        regPanel.setOpaque(false);
        regPanel.setBounds(250, 50, 500, 600); 
        bgPanel.add(regPanel);

        JLabel header = new JLabel(registrationRole.toUpperCase() + " REGISTRATION", SwingConstants.CENTER);
        header.setBounds(0, 20, 500, 30);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        regPanel.add(header);

        int lblX = 50, txtX = 180, width = 270, height = 30, gap = 45;
        int y = 70;

        regPanel.add(new JLabel("Full Name:")).setBounds(lblX, y, 100, height);
        txtName = new JTextField(); txtName.setBounds(txtX, y, width, height);
        regPanel.add(txtName); y += gap;

        regPanel.add(new JLabel("Age:")).setBounds(lblX, y, 100, height);
        txtAge = new JTextField(); txtAge.setBounds(txtX, y, width, height);
        regPanel.add(txtAge); y += gap;

        regPanel.add(new JLabel("Gender:")).setBounds(lblX, y, 100, height);
        genderBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        genderBox.setBounds(txtX, y, width, height);
        regPanel.add(genderBox); y += gap;

        regPanel.add(new JLabel("Blood Group:")).setBounds(lblX, y, 100, height);
        bloodBox = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"});
        bloodBox.setBounds(txtX, y, width, height);
        regPanel.add(bloodBox); y += gap;

        regPanel.add(new JLabel("Phone No:")).setBounds(lblX, y, 100, height);
        txtPhone = new JTextField(); txtPhone.setBounds(txtX, y, width, height);
        regPanel.add(txtPhone); y += gap;

        regPanel.add(new JLabel("Address:")).setBounds(lblX, y, 100, height);
        txtAddress = new JTextField(); txtAddress.setBounds(txtX, y, width, height);
        regPanel.add(txtAddress); y += gap;

        regPanel.add(new JLabel("Username:")).setBounds(lblX, y, 100, height);
        txtUser = new JTextField(); txtUser.setBounds(txtX, y, width, height);
        regPanel.add(txtUser); y += gap;

        regPanel.add(new JLabel("Password:")).setBounds(lblX, y, 100, height);
        txtPass = new JPasswordField(); txtPass.setBounds(txtX, y, width, height);
        regPanel.add(txtPass);

        // Submit Button with rounded corners to match your UI theme
        JButton btnSubmit = new JButton("Create Account") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(180, 25, 25));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnSubmit.setBounds(150, 530, 200, 45);
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setContentAreaFilled(false);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setBorderPainted(false);
        regPanel.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try (Connection conn = DbConnection.connect()) {
                String sql = "INSERT INTO users (name, age, gender, blood_group, phone, address, username, password, role) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtName.getText());
                ps.setInt(2, Integer.parseInt(txtAge.getText()));
                ps.setString(3, (String) genderBox.getSelectedItem());
                ps.setString(4, (String) bloodBox.getSelectedItem());
                ps.setString(5, txtPhone.getText());
                ps.setString(6, txtAddress.getText());
                ps.setString(7, txtUser.getText());
                ps.setString(8, new String(txtPass.getPassword()));
                ps.setString(9, registrationRole); // Use the role passed to the constructor

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Registration Successful!");
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
        
        setVisible(true);
    }
}
