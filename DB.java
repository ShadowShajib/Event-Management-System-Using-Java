import java.sql.*;

public class DB {
    public static Connection getConnection() throws Exception {
        String url = "jdbc:mysql://localhost:3306/event_management";
        String user = "root";
        String pass = "";

        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, pass);
    }
}
