import javax.swing.*;
import java.awt.*;

public class loginframe {

    public static void main(String[] args) {

        JFrame frame = new JFrame("BLOOD BANK MANAGEMENT SYSTEM");
        frame.setSize(1000,700);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

  ImageIcon bgIcon = new ImageIcon("BBMS_BACKGROUND.jpeg");
        Image bgImage = bgIcon.getImage();
        // Background Panel with Image
         JPanel panel = new JPanel() {

            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Draw background image
                g2d.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);

                // Gradient overlay
                GradientPaint gp = new GradientPaint(
                        0,0,new Color(255,230,230,180),
                        0,getHeight(),new Color(255,255,255,180)
                );

                g2d.setPaint(gp);
                g2d.fillRect(0,0,getWidth(),getHeight());
            }
        };

        panel.setBounds(0,0,1000,695);
        panel.setLayout(null);

    panel.setLayout(null);

        panel.setLayout(null);
        frame.setContentPane(panel);

        JLabel title = new JLabel("BLOOD BANK MANAGEMENT SYSTEM");
        title.setBounds(240,40,600,40);
        title.setFont(new Font("Arial",Font.BOLD,28));
        title.setForeground(new Color(150,0,0));
        panel.add(title);

        JPanel loginPanel = new JPanel();
        loginPanel.setBounds(350,200,300,300);
        loginPanel.setLayout(null);
       loginPanel.setBackground(new Color(232,206,206));

        panel.add(loginPanel);

        JLabel loginLabel = new JLabel("LOGIN");
        loginLabel.setBounds(120,20,100,30);
        loginLabel.setFont(new Font("Arial",Font.BOLD,20));
        loginPanel.add(loginLabel);
        loginLabel.setForeground(new Color(80,80,80));
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(30,80,80,25);
        loginPanel.add(userLabel);

        JTextField username = new JTextField();
        username.setBounds(120,80,140,25);
        loginPanel.add(username);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setBounds(30,120,80,25);
        loginPanel.add(passLabel);

        JPasswordField password = new JPasswordField();
        password.setBounds(120,120,140,25);
        loginPanel.add(password);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setBounds(30,160,80,25);
        loginPanel.add(roleLabel);

        String roles[] = {"Select Role","Admin","User","Hospital"};
        JComboBox<String> roleBox = new JComboBox<>(roles);
        roleBox.setBounds(120,160,140,25);
        loginPanel.add(roleBox);

        JButton loginBtn = new JButton("Login");
        loginBtn.setBounds(40,220,90,30);
        loginBtn.setBackground(new Color(200,0,0));
        loginBtn.setForeground(Color.WHITE);
        loginPanel.add(loginBtn);

        JButton registerBtn = new JButton("Register");
        registerBtn.setBounds(150,220,100,30);
        loginPanel.add(registerBtn);

        frame.setVisible(true);
    }
}
