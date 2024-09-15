package org.example.work1;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterController {

    @FXML
    private TextField tfUsername, tfEmail, tfPhoneNumber, tfAddress, tfPAN, tfBankDetails, ifscField;

    @FXML
    private DatePicker tfDOB;

    @FXML
    private PasswordField tfPassword, confirmPasswordField;

    @FXML
    private ComboBox<String> cbRole;  // ComboBox for user type (Agent/Client)

    // Event handler for the submit button
    @FXML
    public void handleSubmit() {
        String username = tfUsername.getText();
        String password = tfPassword.getText();
        String confirmPassword = confirmPasswordField.getText();
        String email = tfEmail.getText();
        String phone_number = tfPhoneNumber.getText();
        String address = tfAddress.getText();
        String pan = tfPAN.getText();
        String bank_account_number = tfBankDetails.getText();
        String ifsc_code = ifscField.getText();

        // Fetch the date from DatePicker
        LocalDate dob = tfDOB.getValue();
        String user_type = cbRole.getValue(); // Fetch the user type

        // Input validation
        if (!validatePassword(password)) {
            showAlert(Alert.AlertType.ERROR, "Weak Password", "Password must be at least 8 characters long, contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
            return;
        }

        if (!checkPasswordMatch(password, confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Password Mismatch", "The passwords do not match. Please re-enter the password.");
            return;
        }

        if (user_type == null || (!user_type.equals("agent") && !user_type.equals("client"))) {
            showAlert(Alert.AlertType.ERROR, "Invalid Role", "Please select a valid role: Agent or Client.");
            return;
        }

        try {
            // Register the user into the database and link to either agent or client table
            registerUser(username, password, email, phone_number, address, pan, bank_account_number, ifsc_code, dob, user_type);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Registration Failed", "There was an error during registration: " + e.getMessage());
        }
    }

    // Method to register the user into the database and link to agent or client table
    public void registerUser(String username, String password, String email, String phone_number,
                             String address, String pan, String bank_account_number, String ifsc_code,
                             LocalDate dob, String user_type) throws SQLException {

        Connection connection = DatabaseConnection.getConnection();

        // Insert into the users table
        String userQuery = "INSERT INTO users (username, password, email, phone_number, address, pan, " +
                "bank_account_number, ifsc_code, date_of_birth, user_type) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(userQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setString(3, email);
            statement.setString(4, phone_number);
            statement.setString(5, address);
            statement.setString(6, pan);
            statement.setString(7, bank_account_number);
            statement.setString(8, ifsc_code);
            statement.setDate(9, java.sql.Date.valueOf(dob));  // Assuming dob is LocalDate
            statement.setString(10, user_type);

            // Execute the update and retrieve the generated user ID
            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // Retrieve the generated user ID (auto-incremented)
            int userid = 0;
            try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userid = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }

            // Insert into the agent or client table based on user type
            if (user_type.equals("agent")) {
                int agent_id = 0;
                try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        agent_id = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }  // Generate unique agent_id
                String agentQuery = "INSERT INTO agents (agent_id, user_id) VALUES (?, ?)";
                try (PreparedStatement agentStatement = connection.prepareStatement(agentQuery)) {
                    agentStatement.setInt(1, agent_id);
                    agentStatement.setInt(2, userid);
                    agentStatement.executeUpdate();
                }
            } else if (user_type.equals("client")) {
                int client_id = 0;
                try (java.sql.ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        client_id = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating user failed, no ID obtained.");
                    }
                }  // Generate unique client_id
                String clientQuery = "INSERT INTO clients (client_id, user_id) VALUES (?, ?)";
                try (PreparedStatement clientStatement = connection.prepareStatement(clientQuery)) {
                    clientStatement.setInt(1, client_id);
                    clientStatement.setInt(2, userid);
                    clientStatement.executeUpdate();
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Registration Success", "User registered successfully with ID: " + userid);
        }
    }


    // Method to validate password strength
    private boolean validatePassword(String password) {
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$";
        Pattern pattern = Pattern.compile(passwordRegex);
        return pattern.matcher(password).matches();
    }

    // Method to check if password and confirm password match
    private boolean checkPasswordMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    // Method to show alerts
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
