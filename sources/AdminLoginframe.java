import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AdminLoginFrame extends JFrame {
    private Image bgImage;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private String selectedRole; 

    public AdminLoginFrame(String role) {
        this.selectedRole = role;
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BLOOD BANK MANAGEMENT SYSTEM - ADMIN PORTAL");
        setSize(1000, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        JLabel title = new JLabel("BLOOD BANK MANAGEMENT SYSTEM", SwingConstants.CENTER);
        title.setBounds(0, 100, 1000, 50);
        title.setFont(new Font("Arial", Font.BOLD, 36));
        title.setForeground(new Color(160, 25, 25)); 
        bgPanel.add(title);

        JPanel loginPanel = new JPanel(null) {
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
        loginPanel.setOpaque(false);
        loginPanel.setBounds(320, 210, 360, 320); 
        bgPanel.add(loginPanel);

        JLabel loginHeader = new JLabel("ADMIN LOGIN", SwingConstants.CENTER);
        loginHeader.setBounds(0, 35, 360, 30);
        loginHeader.setFont(new Font("Arial", Font.BOLD, 24));
        loginPanel.add(loginHeader);

        loginPanel.add(new JLabel("Username:")).setBounds(45, 105, 100, 30);
        txtUser = new JTextField();
        txtUser.setBounds(140, 105, 170, 32);
        loginPanel.add(txtUser);

        loginPanel.add(new JLabel("Password:")).setBounds(45, 155, 100, 30);
        txtPass = new JPasswordField();
        txtPass.setBounds(140, 155, 170, 32);
        loginPanel.add(txtPass);

        // --- LOGIN BUTTON (Centered) ---
        JButton btnLogin = new JButton("Login");
        // Adjusted bounds to center the button since "Register" is removed
        btnLogin.setBounds(100, 230, 160, 40); 
        btnLogin.setBackground(new Color(180, 25, 25));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 16));
        btnLogin.setFocusPainted(false);
        loginPanel.add(btnLogin);

        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Connection conn = DbConnection.connect()) {
                String query = "SELECT user_id, role FROM users WHERE username = ? AND password = ? AND role = 'Admin'";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, user);
                pst.setString(2, pass);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Admin Login Successful!");
                    this.dispose(); 
                    new AdminDashboard().setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Admin Credentials", "Access Denied", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        setVisible(true);
    }
}
