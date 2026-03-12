import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame {
    private Image bgImage;

    public MainWindow() {
        // Using your project's background image
        bgImage = new ImageIcon("BBMS_BACKGROUND.jpeg").getImage();

        setTitle("BBMS - Welcome");
        setSize(1100, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Background Panel with Overlay
        JPanel bgPanel = new JPanel(null) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                // Soft white overlay for readability
                g.setColor(new Color(255, 255, 255, 100)); 
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        setContentPane(bgPanel);

        // --- TITLE ---
        JLabel title = new JLabel("Select Your Login", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(new Color(150, 0, 0)); // Matches your theme
        title.setBounds(0, 150, 1100, 50);
        bgPanel.add(title);

        // --- BUTTON CONTAINER ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 0));
        buttonPanel.setBounds(0, 300, 1100, 200);
        buttonPanel.setOpaque(false);

        // Create the three required buttons
        JButton btnUser = createRoleButton("User", "user_icon.png");
        JButton btnAdmin = createRoleButton("Admin", "admin_icon.png");
        JButton btnHospital = createRoleButton("Hospital", "hospital_icon.png");

        buttonPanel.add(btnUser);
        buttonPanel.add(btnAdmin);
        buttonPanel.add(btnHospital);
        bgPanel.add(buttonPanel);

        // --- ACTION LISTENERS ---
        btnUser.addActionListener(e -> openUserLogin("User"));
        btnAdmin.addActionListener(e -> openAdminLogin("Admin"));
        btnHospital.addActionListener(e -> openHospitalLogin("Hospital"));
    }

    private JButton createRoleButton(String text, String iconPath) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Rounded corner style to match your request blood button
                g2.setColor(new Color(180, 0, 0)); 
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 30, 30);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setPreferredSize(new Dimension(200, 100));
        btn.setFont(new Font("Arial", Font.BOLD, 22));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return btn;
    }

    private void openUserLogin(String role) {
        this.dispose();
        // Passing the selected role to your existing LoginFrame
        new UserLoginFrame(role).setVisible(true); 
    }

    private void openAdminLogin(String role) {
        this.dispose();
        // Passing the selected role to your existing LoginFrame
        new AdminLoginFrame(role).setVisible(true); 
    }
    private void openHospitalLogin(String role) {
        this.dispose();
        // Passing the selected role to your existing LoginFrame
        new HospitalLoginFrame(role).setVisible(true); 
    }


    public static void main(String[] args) {
        new MainWindow().setVisible(true);
    }
}
