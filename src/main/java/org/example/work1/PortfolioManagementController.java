package org.example.work1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class PortfolioManagementController {

    @FXML
    private TableView<MutualFund2> investmentTable;

    @FXML
    private TableColumn<MutualFund2, String> schemeCodeColumn;
    @FXML
    private TableColumn<MutualFund2, String> schemeNameColumn;
    @FXML
    private TableColumn<MutualFund2, Double> navColumn;
    @FXML
    private TableColumn<MutualFund2, Double> amountInvestedColumn;
    @FXML
    private TableColumn<MutualFund2, Double> currentValueColumn;
    @FXML
    private TableColumn<MutualFund2, Double> costperunitColumn;
    @FXML
    private TableColumn<MutualFund2, Double> unitsColumn;
    @FXML
    private TextArea amountInvestedTextArea;
    @FXML
    private TextArea currentValueTextArea;

    private Stage stage;
    private Scene scene;
    private Parent root;

    private ObservableList<MutualFund2> mutualFundsList = FXCollections.observableArrayList();

    public void initialize() {
        if (schemeCodeColumn != null) {
            schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        } else {
            System.out.println("Error: schemeCodeColumn is null");
        }
        // Initialize table columns
        schemeCodeColumn.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        schemeNameColumn.setCellValueFactory(new PropertyValueFactory<>("schemeName"));
        navColumn.setCellValueFactory(new PropertyValueFactory<>("nav"));
        amountInvestedColumn.setCellValueFactory(new PropertyValueFactory<>("amountInvested"));
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        costperunitColumn.setCellValueFactory(new PropertyValueFactory<>("costPerUnit"));
        unitsColumn.setCellValueFactory(new PropertyValueFactory<>("units"));

        // Load the data from database
        loadTableData();
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    public void loadPortfolioData(int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String query = "SELECT amount_invested, current_value FROM portfolio WHERE user_id = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // Set the values in the TextAreas
                double amountInvested = resultSet.getDouble("amount_invested");
                double currentValue = resultSet.getDouble("current_value");

// Format to 4 decimal places
                String formattedCurrentValue = String.format("%.4f", currentValue);

// If you want to use the value elsewhere as a double
                double roundedCurrentValue = Double.parseDouble(formattedCurrentValue);

                amountInvestedTextArea.setText(String.valueOf(amountInvested));
                currentValueTextArea.setText(String.valueOf(roundedCurrentValue));
            }

            resultSet.close();
            preparedStatement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void loadTableData() {
        // Load data from the database into TableView
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            String query = "SELECT scheme_code, fund_name, nav, amount_invested, current_value, units, costperunit FROM mutual_funds";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                String schemeName = resultSet.getString("fund_name");
                double nav = resultSet.getDouble("nav");
                double amountInvested = resultSet.getDouble("amount_invested");
                double currentValue = resultSet.getDouble("current_value");
                double units = resultSet.getDouble("units");
                double costPerUnit = resultSet.getDouble("costperunit");

                // Add data to the list
                MutualFund2 mutualFund = new MutualFund2(schemeCode, schemeName, nav, amountInvested, currentValue, units, costPerUnit);
                mutualFundsList.add(mutualFund);
            }

            // Set the items for the TableView
            investmentTable.setItems(mutualFundsList);
            int userId = getCurrentUserId();
            loadPortfolioData(userId);
            // Close resources
            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            System.err.println("SQL Error:");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
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
