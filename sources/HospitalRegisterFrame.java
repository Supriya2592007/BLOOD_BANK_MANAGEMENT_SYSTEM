import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class HospitalRegisterFrame extends JFrame {
    private Image bgImage;
    private JTextField txtHospName, txtLocation, txtContact, txtUser;
    private JPasswordField txtPass;
    private String registrationRole; 

    // This constructor MUST accept a String to match your LoginFrame call
    public HospitalRegisterFrame(String role) {
        this.registrationRole = role;
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - HOSPITAL REGISTRATION");
        setSize(1000, 700);
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
        regPanel.setBounds(250, 80, 500, 520); 
        bgPanel.add(regPanel);

        JLabel header = new JLabel("HOSPITAL REGISTRATION", SwingConstants.CENTER);
        header.setBounds(0, 30, 500, 30);
        header.setFont(new Font("Arial", Font.BOLD, 24));
        header.setForeground(new Color(160, 25, 25));
        regPanel.add(header);

        int lblX = 50, txtX = 200, width = 240, height = 32, gap = 55;
        int y = 100;

        // Using columns from your 'hospitals' table
        regPanel.add(new JLabel("Hospital Name:")).setBounds(lblX, y, 120, height);
        txtHospName = new JTextField(); 
        txtHospName.setBounds(txtX, y, width, height);
        regPanel.add(txtHospName); y += gap;

        regPanel.add(new JLabel("Location:")).setBounds(lblX, y, 120, height);
        txtLocation = new JTextField(); 
        txtLocation.setBounds(txtX, y, width, height);
        regPanel.add(txtLocation); y += gap;

        regPanel.add(new JLabel("Contact No:")).setBounds(lblX, y, 120, height);
        txtContact = new JTextField(); 
        txtContact.setBounds(txtX, y, width, height);
        regPanel.add(txtContact); y += gap;

        regPanel.add(new JLabel("Username:")).setBounds(lblX, y, 120, height);
        txtUser = new JTextField(); 
        txtUser.setBounds(txtX, y, width, height);
        regPanel.add(txtUser); y += gap;

        regPanel.add(new JLabel("Password:")).setBounds(lblX, y, 120, height);
        txtPass = new JPasswordField(); 
        txtPass.setBounds(txtX, y, width, height);
        regPanel.add(txtPass);

        JButton btnSubmit = new JButton("Register Hospital");
        btnSubmit.setBounds(150, 430, 200, 45);
        btnSubmit.setBackground(new Color(180, 25, 25));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 16));
        btnSubmit.setFocusPainted(false);
        regPanel.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            try (Connection conn = DbConnection.connect()) {
                String sql = "INSERT INTO hospitals (hospital_name, location, contact_number, username, password) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, txtHospName.getText());
                ps.setString(2, txtLocation.getText());
                ps.setString(3, txtContact.getText());
                ps.setString(4, txtUser.getText());
                ps.setString(5, new String(txtPass.getPassword()));

                ps.executeUpdate();
                JOptionPane.showMessageDialog(this, "Hospital Registered Successfully!");
                this.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });
    }
}
