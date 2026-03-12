import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class HospitalLoginFrame extends JFrame {
    private Image bgImage;
    private JTextField txtUser;
    private JPasswordField txtPass;
    private String selectedRole; // Passed as "Hospital" from MainWindow

    public HospitalLoginFrame(String role) {
        this.selectedRole = role;
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BLOOD BANK MANAGEMENT SYSTEM - HOSPITAL PORTAL");
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

        JLabel loginHeader = new JLabel("HOSPITAL LOGIN", SwingConstants.CENTER);
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

        // --- AUTHENTICATION LOGIC ---
        JButton btnLogin = new JButton("Login");
        btnLogin.setBounds(65, 240, 105, 38);
        btnLogin.setBackground(new Color(180, 25, 25));
        btnLogin.setForeground(Color.WHITE);
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
                // Modified to use the 'hospitals' table structure
                String query = "SELECT hospital_id, hospital_name FROM hospitals WHERE username = ? AND password = ?";
                PreparedStatement pst = conn.prepareStatement(query);
                pst.setString(1, user);
                pst.setString(2, pass);

                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    int hId = rs.getInt("hospital_id"); // Correct column from table
                    String hName = rs.getString("hospital_name");

                    JOptionPane.showMessageDialog(this, "Login Successful! Welcome " + hName);
                    this.dispose(); 
                    
                    // Passing hospital ID and name to the dashboard
                   new HospitalDashboard(hId, hName).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid Hospital Credentials", "Login Failed", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // --- REGISTER BUTTON ---
        JButton btnReg = new JButton("Register");
        btnReg.setBounds(190, 240, 105, 38);
        btnReg.setFocusPainted(false);
        loginPanel.add(btnReg);
        

            // Change this line in your HospitalLoginFrame
btnReg.addActionListener(e -> new HospitalRegisterFrame("Hospital").setVisible(true));
    }
}
