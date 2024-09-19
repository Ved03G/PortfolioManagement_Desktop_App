package org.example.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.IOException;
import java.util.regex.Pattern;
public class LoginController {

    @FXML
    private TextField tfusername;

    @FXML
    private PasswordField tfpass;

    @FXML
    private Button btnLogin;

    @FXML
    private Button btnRegister;

    @FXML
    private ComboBox<String> cbRole;

    // Event handler for the login button
    @FXML
//    private void handleLoginButtonClick(ActionEvent event) {
//        String username = tfusername.getText();
//        String password = tfpass.getText();
//        String requiredRole = cbRole.getValue();
//
//        try {
//            // Check if credentials are valid using database validation
//            if (validateLogin(username, password, requiredRole)) {
//                // Redirect to Client Dashboard if login is successful
//                loadClientDashboard();
//            } else {
//                // Show an alert if login fails
//                Alert alert = new Alert(AlertType.ERROR);
//                alert.setTitle("Login Failed");
//                alert.setHeaderText(null);
//                alert.setContentText("Invalid username or password or role. Please try again.");
//                alert.showAndWait();
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            Alert alert = new Alert(AlertType.ERROR);
//            alert.setTitle("Error");
//            alert.setHeaderText(null);
//            alert.setContentText("An error occurred while trying to log in. Please try again.");
//            alert.showAndWait();
//        }
//    }
//
//    // Method to validate login with the database
//    private boolean validateLogin(String username, String password, String requiredRole) throws SQLException {
//        Connection connection = DatabaseConnection.getConnection();
//
//        String query = "SELECT * FROM users WHERE username = ? AND password = ? ";
//        try (PreparedStatement statement = connection.prepareStatement(query)) {
//            statement.setString(1, username);
//            statement.setString(2, password);  // Use hashed password in real scenarios
//            // Check for the required role (client or agent)
//
//            ResultSet resultSet = statement.executeQuery();
//
//            if (resultSet.next()) {
//                System.out.println("Login successful!");
//                int userId = resultSet.getInt("userid");
//                UserSession.getInstance(userId);  // Store user ID in session
//                return true;  // User found and role matches
//            } else {
//                System.out.println("Invalid username, password, or role.");
//                return false;  // User not found or role doesn't match
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            if (connection != null) {
//                connection.close();
//            }
//        }
//    }


    // Method to load the Client Dashboard page after successful login
    private void loadClientDashboard() {
        try {
            // Load the Client Dashboard FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PortfolioManagement.fxml"));
            Parent root = loader.load();

            // Get the current stage and set the scene to Client Dashboard
            Stage stage = (Stage) btnLogin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Client Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Event handler for the register button
    @FXML
    private void HandleRegisterClick(ActionEvent event) throws IOException {
        // Load Register.fxml
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
        Parent root = loader.load();

        // Get current stage
        Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

        // Set the scene to Register page
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Register Page");
        stage.show();
    }
}
