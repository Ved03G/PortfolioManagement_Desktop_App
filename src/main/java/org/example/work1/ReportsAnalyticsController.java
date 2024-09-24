package org.example.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ReportsAnalyticsController {

    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private PieChart portfolioPieChart;

    @FXML
    private LineChart<String, Number> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

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
    @FXML
    public void initialize() {
        // Initialize PieChart data from the database
        fetchDataAndPopulatePieChart();

        // Initialize LineChart data
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Series 1");
        series.getData().add(new XYChart.Data<>("Jan", 23));
        series.getData().add(new XYChart.Data<>("Feb", 14));
        series.getData().add(new XYChart.Data<>("Mar", 15));
        series.getData().add(new XYChart.Data<>("Apr", 24));
        series.getData().add(new XYChart.Data<>("May", 34));
        series.getData().add(new XYChart.Data<>("Jun", 36));

        lineChart.getData().add(series);
    }
    private void fetchDataAndPopulatePieChart() {
        Connection connection = null;
        Statement statement = null;

        try {
            // Connect to the database (replace with your database credentials)
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/PortfolioDB", "root", "Omkar@16905");

            // Create a statement
            statement = connection.createStatement();

            // Execute the query to get the type data
            String query = "SELECT  COUNT(*) AS Count FROM portfolio ";
            ResultSet resultSet = statement.executeQuery(query);

            // Populate the PieChart with data from the result set
            while (resultSet.next()) {
                String type = resultSet.getString("Type");
                int count = resultSet.getInt("Count");

                // Add data to PieChart
                PieChart.Data slice = new PieChart.Data(type, count);
                portfolioPieChart.getData().add(slice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close the database resources
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

