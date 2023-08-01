package Console;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class user {
    public static void main(String[] args) {
        // Database connection 
        String jdbcUrl = "jdbc:mysql://localhost:3306/login"; 
        String username = "root";
        String password = "root";

        // Step 1: Load the JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: MySQL JDBC driver not found.");
            e.printStackTrace();
            return;
        }

        // Step 2: Establish the connection
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(jdbcUrl, username, password);
            System.out.println("Connected to the database.");
            // Here you can execute queries or statements on the database
        } catch (SQLException e) {
            System.err.println("Error: Unable to connect to the database.");
            e.printStackTrace();
        } finally {
            // Step 5: Close the connection (optional, but recommended)
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("Connection closed.");
                } catch (SQLException e) {
                    System.err.println("Error: Failed to close the connection.");
                    e.printStackTrace();
                }
            }
        }
    }
}
