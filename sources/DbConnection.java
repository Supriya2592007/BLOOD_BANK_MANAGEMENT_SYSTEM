import java.sql.*;

public class DbConnection {
    public static Connection connect() {
        try {
            // Replace 'root' and 'your_password' with your MySQL details
            Class.forName("com.mysql.cj.jdbc.Driver");
        //get connection
        String url,user_name,password;
        //command to enter in cmd ---> mysql -u root -p
        //password:password given for mysql installer
        //Create database --> create database jdbcdb;
       url="jdbc:mysql://localhost:3306/blood_service_system";
        user_name="root";
       password="Sai2007@))&";
      Connection con = DriverManager.getConnection(url,user_name,password);
       return con;
        } catch (Exception e) {
            System.out.println("Database Connection Failed: " + e.getMessage());
            return null;
        }
    }
}
