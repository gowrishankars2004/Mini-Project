package Console;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Exit implements Printable {
    @Override
    public void print() {
        System.out.println("Thank you for using our Salon Booking System.");
    }
}

class Message implements Printable {
    @Override
    public void print() {
        System.out.println("Have a great day. We hope to see you again soon!");
    }
}

interface Printable {
    void print();
}

// abstract class JdbcConfiguration {
//     protected static String jdbcUrl = "jdbc:mysql://localhost:3306/login";
//     protected static String username = "root";
//     protected static String password = "root";
// }

class SelectService extends JdbcConfiguration {

    public static void main(String[] args) throws ClassNotFoundException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password)) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Welcome to the Salon Booking System!");
            System.out.print("Enter your UserName: ");
            String userName = sc.nextLine();

            System.out.println("\nhi " + userName + " welcome to our saloon and spa application.");

            // Additional requirements
            viewAvailableServices(connection);

            int option;
            while (true) {
                displayMainMenu();
                System.out.print("Enter your choice: ");
                option = sc.nextInt();

                switch (option) {
                    case 1:
                        bookAppointmentProcess(connection, sc, userName);
                        break;
                    case 2:
                        viewBookedAppointments(connection, userName);
                        break;
                    case 3:
                        generateBill(connection, userName);
                        break;
                    case 4:
                        cancelAppointment(connection, userName);
                        break;
                    case 5:
                        giveFeedback(connection, userName);
                        break;
                    case 6:
                        Printable printable = new Message();
                        printable.print();
                        break;
                    case 7:
                        Printable exitPrintable = new Exit();
                        exitPrintable.print();
                        break;
                    default:
                        System.out.println("\nInvalid choice. Please try again.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Error: Unable to connect to the database");
            e.printStackTrace();
        }
    }

    // Service class to represent a service
    private static class Service {
        private int serviceId;
        private String name;
        private String description;
        private double price;

        public Service(int serviceId, String name, String description, double price) {
            this.serviceId = serviceId;
            this.name = name;
            this.description = description;
            this.price = price;
        }

        // // Getters for the properties
        // public int getServiceId() {
        //     return serviceId;
        // }

        // public String getName() {
        //     return name;
        // }

        // public String getDescription() {
        //     return description;
        // }

        // public double getPrice() {
        //     return price;
        // }

        // Override toString method for printing the service details
        @Override
        public String toString() {
            return serviceId + ". " + name + " - " + description + " - $" + price;
        }
    }

    // Method to check if the selected service exists
    private static boolean checkServiceExists(Connection connection, int serviceId) throws SQLException {
        String selectServiceQuery = "SELECT * FROM service WHERE serviceId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectServiceQuery)) {
            preparedStatement.setInt(1, serviceId);
            ResultSet selectedResult = preparedStatement.executeQuery();
            return selectedResult.next();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
            return false;
        }
    }

    // Method to select a beautician
    private static int selectBeautician(Connection connection) {
        Scanner sc = new Scanner(System.in);
        viewConsultantList(connection);
        System.out.print("\nEnter the beautician ID to select: ");
        int selectedBeauticianId = sc.nextInt();
        sc.close();
        if (checkBeauticianExists(connection, selectedBeauticianId)) {
            System.out.println("Beautician selected successfully!");
            return selectedBeauticianId;
        } else {
            
            System.out.println("\nBeautician not found for the given ID.");
            return -1;
        }
    }

    // Method to check if the selected beautician exists
    private static boolean checkBeauticianExists(Connection connection, int beauticianId) {
        String selectBeauticianQuery = "SELECT * FROM beauticians WHERE consultantId = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectBeauticianQuery)) {
            preparedStatement.setInt(1, beauticianId);
            ResultSet selectedResult = preparedStatement.executeQuery();
            return selectedResult.next();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
            return false;
        }
    }

    // Method to book an appointment with the selected beautician
    private static void bookAppointment(Connection connection, String userName, int serviceId, String appointmentDate,
            String appointmentTime, int beauticianId) {
        String insertAppointmentQuery = "INSERT INTO appointments (UserName, ServiceId, AppointmentDate, AppointmentTime, BeauticianId) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertAppointmentQuery)) {
            preparedStatement.setString(1, userName);
            preparedStatement.setInt(2, serviceId);
            preparedStatement.setString(3, appointmentDate);
            preparedStatement.setString(4, appointmentTime);
            preparedStatement.setInt(5, beauticianId);
            preparedStatement.executeUpdate();
            System.out.println("\nAppointment booked successfully!");
        } catch (SQLException e) {
            System.err.println("Error: Unable to book the appointment");
            e.printStackTrace();
        }
    }

    // Method to generate a detailed bill
    private static double generateBill(Connection connection, String userName) {
        double totalPrice = 0.0;
        String selectAppointmentsQuery = "SELECT a.AppointmentId, s.name, s.Price, b.Name AS BeauticianName "
                + "FROM appointments a "
                + "INNER JOIN service s ON a.ServiceId = s.serviceId "
                + "INNER JOIN beauticians b ON a.BeauticianId = b.consultantId "
                + "WHERE a.UserName = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsQuery)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();
            System.out.println("\nDetailed Bill for " + userName);
            System.out.println("AppointmentID - Service Name - Beautician Name - Price");
            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("AppointmentId");
                String serviceName = resultSet.getString("name");
                double price = resultSet.getDouble("Price");
                String beauticianName = resultSet.getString("BeauticianName");

                System.out.println(appointmentId + " - " + serviceName + " - " + beauticianName + " - $" + price);
                totalPrice += price;
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve booked appointments");
            e.printStackTrace();
        }
        return totalPrice;
    }

    // Method to view a list of consultants (beauticians)
    private static void viewConsultantList(Connection connection) {
        String selectConsultantsQuery = "SELECT * FROM beauticians";
        try (Statement statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery(selectConsultantsQuery)) {
            System.out.println("\nList of Beauticians");
            while (resultSet.next()) {
                int consultantId = resultSet.getInt("consultantId");
                String consultantName = resultSet.getString("Name");
                String specialization = resultSet.getString("Specialization");
                System.out.println(consultantId + ". " + consultantName + " - " + specialization);
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve consultant list");
            e.printStackTrace();
        }
    }

    // Method for users to give feedback
    private static void giveFeedback(Connection connection, String userName) {
        Scanner sc = new Scanner(System.in);
        System.out.print("\nDo you want to give feedback? (yes/no): ");
        String feedbackOption = sc.nextLine().trim().toLowerCase();
        if (feedbackOption.equals("yes")) {
            System.out.print("Enter your feedback: ");
            String feedbackMessage = sc.nextLine();
            sc.close();
            String insertFeedbackQuery = "INSERT INTO feedback (UserName, FeedbackMessage) VALUES (?, ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertFeedbackQuery)) {
                preparedStatement.setString(1, userName);
                preparedStatement.setString(2, feedbackMessage);
                preparedStatement.executeUpdate();
                System.out.println("Thank you for your feedback!");
            } catch (SQLException e) {
                System.err.println("Error: Unable to submit feedback");
                e.printStackTrace();
            }
        } else {
            System.out.println("Thank you");
        }
    }

    // Helper method to display the main menu options
    private static void displayMainMenu() {
        System.out.println("\nMain Menu");
        System.out.println("1. Book an Appointment");
        System.out.println("2. View Booked Appointments");
        System.out.println("3. Generate Detailed Bill");
        System.out.println("4. Cancel Appointment");
        System.out.println("5. Give Feedback");
        System.out.println("6. Exit");
    }

    // Method to view available services using abstraction
    private static void viewAvailableServices(Connection connection) {
        String selectQuery = "SELECT * FROM service";
        List<Service> availableServices = new ArrayList<>();

        try (Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(selectQuery)) {
            while (rs.next()) {
                int serviceId = rs.getInt("serviceId");
                String name = rs.getString("name");
                String description = rs.getString("Description");
                double price = rs.getDouble("Price");

                // Create a Service object and add it to the list
                availableServices.add(new Service(serviceId, name, description, price));
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
        }

        System.out.println("\nAvailable Services");
        for (Service service : availableServices) {
            System.out.println(service);
        }
    }

    // Method to book an appointment process
    private static void bookAppointmentProcess(Connection connection, Scanner sc, String userName) {
        try {
            // Display available services
            viewAvailableServices(connection);

            // Get the service ID from the user
            System.out.print("\nEnter the Service ID to book an appointment: ");
            int serviceId = sc.nextInt();

            // Check if the selected service exists
            if (checkServiceExists(connection, serviceId)) {
                // Get appointment date and time from the user
                System.out.print("Enter the Appointment Date (YYYY-MM-DD): ");
                String appointmentDate = sc.next();
                System.out.print("Enter the Appointment Time (HH:MM): ");
                String appointmentTime = sc.next();

                // Select a beautician for the appointment
                int beauticianId = selectBeautician(connection);
                if (beauticianId != -1) {
                    // Book the appointment
                    bookAppointment(connection, userName, serviceId, appointmentDate, appointmentTime, beauticianId);
                }
            } else {
                System.out.println("Service not found for the given Service ID.");
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to book the appointment");
            e.printStackTrace();
        }
    }

    // Method to view booked appointments
    private static void viewBookedAppointments(Connection connection, String userName) {
        String selectAppointmentsQuery = "SELECT a.AppointmentId, s.name AS ServiceName, a.AppointmentDate, a.AppointmentTime, "
                + "b.Name AS BeauticianName "
                + "FROM appointments a "
                + "INNER JOIN service s ON a.ServiceId = s.serviceId "
                + "INNER JOIN beauticians b ON a.BeauticianId = b.consultantId "
                + "WHERE a.UserName = ? "
                + "ORDER BY a.AppointmentDate, a.AppointmentTime";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentsQuery)) {
            preparedStatement.setString(1, userName);
            ResultSet resultSet = preparedStatement.executeQuery();

            System.out.println("\nYour Booked Appointments:");
            System.out.println("AppointmentID - Service Name - Appointment Date - Appointment Time - Beautician Name");

            while (resultSet.next()) {
                int appointmentId = resultSet.getInt("AppointmentId");
                String serviceName = resultSet.getString("ServiceName");
                String appointmentDate = resultSet.getString("AppointmentDate");
                String appointmentTime = resultSet.getString("AppointmentTime");
                String beauticianName = resultSet.getString("BeauticianName");

                System.out.println(appointmentId + " - " + serviceName + " - " + appointmentDate + " - "
                        + appointmentTime + " - " + beauticianName);
            }
        } catch (SQLException e) {
            System.err.println("Error: Unable to retrieve booked appointments");
            e.printStackTrace();
        }
    }

    // Method to cancel an appointment
    private static void cancelAppointment(Connection connection, String userName) {
        Scanner sc = new Scanner(System.in);
        viewBookedAppointments(connection, userName);

        System.out.print("\nEnter the Appointment ID to cancel: ");
        int appointmentId = sc.nextInt();
        sc.close();
        if (checkAppointmentExists(connection, userName, appointmentId)) {
            String deleteAppointmentQuery = "DELETE FROM appointments WHERE AppointmentId = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteAppointmentQuery)) {
                preparedStatement.setInt(1, appointmentId);
                preparedStatement.executeUpdate();
                System.out.println("Appointment with ID " + appointmentId + " has been canceled.");
            } catch (SQLException e) {
                System.err.println("Error: Unable to cancel the appointment");
                e.printStackTrace();
            }
        } else {
            System.out.println("\nAppointment not found for the given ID.");
        }
    }

    // Method to check if the appointment exists
    private static boolean checkAppointmentExists(Connection connection, String userName, int appointmentId) {
        String selectAppointmentQuery = "SELECT * FROM appointments WHERE AppointmentId = ? AND UserName = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectAppointmentQuery)) {
            preparedStatement.setInt(1, appointmentId);
            preparedStatement.setString(2, userName);
            ResultSet selectedResult = preparedStatement.executeQuery();
            return selectedResult.next();
        } catch (SQLException e) {
            System.err.println("Error: Unable to execute the query");
            e.printStackTrace();
            return false;
        }
    }
}
