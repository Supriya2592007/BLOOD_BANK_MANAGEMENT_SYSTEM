import java.sql.*;

public class DbConnection {
    public static Connection connect() {
        try {
            // Replace 'root' and 'your_password' with your MySQL details
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/blood_service_system", "root", "Sai2007@))&");
        } catch (Exception e) {
            System.out.println("Database Connection Failed: " + e.getMessage());
            return null;
        }
    }
}
