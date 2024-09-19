package org.example.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import javafx.fxml.Initializable;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class UserProfileController implements Initializable {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TextField usernameField, phoneField, addressField, userTypeField, bankAccountField, ifscField, emailField, panField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private DatePicker dobPicker;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        int userId = getCurrentUserId();
        loadUserData(userId);  // Call the method when the view is initialized
    }
    // Method to load user data
    public void loadUserData(int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT username, password, phone_number, address, date_of_birth, user_type, bank_account_number, ifsc_code, email, pan FROM users WHERE userid = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                usernameField.setText(resultSet.getString("username"));
                passwordField.setText(resultSet.getString("password"));
                phoneField.setText(resultSet.getString("phone_number"));
                addressField.setText(resultSet.getString("address"));
                dobPicker.setValue(resultSet.getDate("date_of_birth").toLocalDate());
                userTypeField.setText(resultSet.getString("user_type"));
                bankAccountField.setText(resultSet.getString("bank_account_number"));
                ifscField.setText(resultSet.getString("ifsc_code"));
                emailField.setText(resultSet.getString("email"));
                panField.setText(resultSet.getString("pan"));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to save updated profile data
    @FXML
    private void saveProfile() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String updateQuery = "UPDATE users SET password = ?, phone_number = ?, address = ?, date_of_birth = ?, bank_account_number = ?, ifsc_code = ?, email = ?, pan = ? WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);

            preparedStatement.setString(1, passwordField.getText());
            preparedStatement.setString(2, phoneField.getText());
            preparedStatement.setString(3, addressField.getText());
            preparedStatement.setDate(4, Date.valueOf(dobPicker.getValue()));
            preparedStatement.setString(5, bankAccountField.getText());
            preparedStatement.setString(6, ifscField.getText());
            preparedStatement.setString(7, emailField.getText());
            preparedStatement.setString(8, panField.getText());
            preparedStatement.setString(9, usernameField.getText());

            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Profile updated successfully!");
            }

            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Edit profile method (can enable fields for editing if necessary)
    @FXML
    private void editProfile() {
        phoneField.setEditable(true);
        addressField.setEditable(true);
        passwordField.setEditable(true);
        dobPicker.setDisable(false);
        bankAccountField.setEditable(true);
        ifscField.setEditable(true);
        emailField.setEditable(true);
        panField.setEditable(true);
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }

    // Method to load the FXML of each section
    private void switchToPage(ActionEvent event, String fxmlFile, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage(event, "PortfolioManagement.fxml", "Portfolio Management");
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage(event, "SIPManagement.fxml", "SIP Management");
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage(event, "MutualFunds.fxml", "Mutual Funds");
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage(event, "ReportsAnalytics.fxml", "Reports & Analytics");
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage(event, "TransactionHistory.fxml", "Transaction History");
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage(event, "UserProfile.fxml", "User Profile");
    }
}
