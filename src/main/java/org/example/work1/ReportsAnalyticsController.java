package org.example.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReportsAnalyticsController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private PieChart portfolioPieChart;

    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private LineChart<String, Number> mutualFundLineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    public void initialize() {
        // Initialize PieChart data
        fetchDataAndPopulatePieChart();

        // Call to show the line chart
        List<String> fundIds = getFundIdsFromDatabase();

        // Assuming you want to display data for the last 5 days
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(5);

        // Fetch and display the daily current values
        showDailyCurrentValueChart(lineChart, fundIds, startDate, endDate);
        showDailyCurrentValueChartForMutualFunds(mutualFundLineChart, fundIds, startDate, endDate);
    }

    private List<String> getFundIdsFromDatabase() {
        List<String> fundIds = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;

        try {
            // Connect to the database
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
            statement = connection.createStatement();

            // Query to fetch fund_id from mutual_funds
            String mutualFundsQuery = "SELECT scheme_code FROM mutual_funds";
            ResultSet resultSetMutualFunds = statement.executeQuery(mutualFundsQuery);

            // Add fund IDs from mutual_funds table to the list
            while (resultSetMutualFunds.next()) {
                String fundId = resultSetMutualFunds.getString("scheme_code");
                fundIds.add(fundId);
            }

            // Query to fetch fund_id from sip
            String sipQuery = "SELECT fund_id FROM sip";
            ResultSet resultSetSip = statement.executeQuery(sipQuery);

            // Add fund IDs from sip table to the list
            while (resultSetSip.next()) {
                String fundId = resultSetSip.getString("fund_id");
                fundIds.add(fundId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close resources
            try {
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return fundIds;
    }

    // Fetch data from the database and populate the PieChart
    private void fetchDataAndPopulatePieChart() {
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/javafxapp", "root", "Servesh#21");
             Statement statement = connection.createStatement()) {

            String query = "SELECT type, SUM(amount_invested) AS Count FROM portfolio GROUP BY type";
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String type = resultSet.getString("type");
                double count = resultSet.getDouble("Count");

                PieChart.Data slice = new PieChart.Data(type, count);
                portfolioPieChart.getData().add(slice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Fetch NAV for a specific date from the API
    public double getNavForDate(String fundId, String date) {
        double nav = 0.0;
        String apiUrl = "https://api.mfapi.in/mf/" + fundId;

        try {
            // Create the URL connection to the API
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Check for a valid response code from the API
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Get the response from the API
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse the JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray data = jsonResponse.getJSONArray("data");

                // Loop through the data array to find the NAV for the specified date
                for (int i = 0; i < data.length(); i++) {
                    JSONObject entry = data.getJSONObject(i);
                    String navDate = entry.getString("date");

                    // Check if the date matches
                    if (navDate.equals(date)) {
                        nav = entry.getDouble("nav");
                        break;
                    }
                }
            } else {
                System.out.println("Error: Received HTTP response code " + responseCode);
            }

        } catch (FileNotFoundException e) {
            System.out.println("Error: Invalid fund ID or data not found for URL: " + apiUrl);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nav;
    }

    // Method to populate the current value line chart
    public void showDailyCurrentValueChart(LineChart<String, Number> lineChart, List<String> fundIds, LocalDate startDate, LocalDate endDate) {
        lineChart.getData().clear();
        XYChart.Series<String, Number> totalValueSeries = new XYChart.Series<>();
        totalValueSeries.setName("Total Current Value");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        // Iterate through each date from startDate to endDate
        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            double totalCurrentValue = 0.0;
            boolean hasInvestment = false;

            // Check each fund ID
            for (String fundId : fundIds) {
                // Get invested amount and total units for the current fund from SIPs
                double investedAmountSIP = getInvestedAmountForFund(fundId);
                double totalUnitsSIP = getTotalUnitsInvestedForFund(fundId);

                // Fetch the NAV for the current date for SIP
                double navSIP = getNavForDate(fundId, currentDate.format(formatter));

                // Only consider SIP fund if it was purchased on or before the current date
                if (isFundPurchasedOnOrBefore(fundId, currentDate) && totalUnitsSIP > 0) {
                    if (navSIP != 0.0) {
                        double currentValueSIP = navSIP * totalUnitsSIP;
                        totalCurrentValue += currentValueSIP;
                    } else {
                        totalCurrentValue += investedAmountSIP; // Use invested amount if NAV is 0
                    }
                    hasInvestment = true; // At least one SIP investment exists for the day
                }

                // Get invested amount and total units for the current fund from mutual funds
                double investedAmountMF = getInvestedAmountForFund(fundId);
                double totalUnitsMF = getTotalUnitsInvestedForFund(fundId);

                // Fetch the NAV for the current date for Mutual Funds
                double navMF = getNavForDate(fundId, currentDate.format(formatter));

                // Only consider mutual fund if it was purchased on or before the current date
                if (isMutualFundPurchasedOnOrBefore(fundId, currentDate) && totalUnitsMF > 0) {
                    if (navMF != 0.0) {
                        double currentValueMF = navMF * totalUnitsMF;
                        totalCurrentValue += currentValueMF;
                    } else {
                        totalCurrentValue += investedAmountMF; // Use invested amount if NAV is 0
                    }
                    hasInvestment = true; // At least one mutual fund investment exists for the day
                }
            }

            // Add data to the chart only if there was an investment on that day
            if (hasInvestment) {
                totalValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalCurrentValue));
            }
        }

        lineChart.getData().add(totalValueSeries);
    }
    public void showDailyCurrentValueChartForMutualFunds(LineChart<String, Number> lineChart, List<String> fundIds, LocalDate startDate, LocalDate endDate) {
        lineChart.getData().clear();
        XYChart.Series<String, Number> totalValueSeries = new XYChart.Series<>();
        totalValueSeries.setName("Total Current Value (Mutual Funds)");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
            double totalCurrentValue = 0.0;
            boolean hasInvestment = false;

            for (String fundId : fundIds) {
                double investedAmountMF = getInvestedAmount(fundId);
                double totalUnitsMF = getTotalValueForFund(fundId);
                double navMF = getNavForDate(fundId, currentDate.format(formatter));

                if (isMutualFundPurchasedOnOrBefore(fundId, currentDate) && totalUnitsMF > 0) {
                    if (navMF != 0.0) {
                        double currentValueMF = navMF * totalUnitsMF;
                        totalCurrentValue += currentValueMF;
                    } else {
                        totalCurrentValue += investedAmountMF;
                    }
                    hasInvestment = true;
                }
            }

            if (hasInvestment) {
                totalValueSeries.getData().add(new XYChart.Data<>(currentDate.format(formatter), totalCurrentValue));
            }
        }

        lineChart.getData().add(totalValueSeries);
    }

    // Method to check if the fund was purchased on or before a given date
    private boolean isFundPurchasedOnOrBefore(String fundId, LocalDate date) {
        boolean purchased = false;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM sip WHERE fund_id = ? AND start_date <= ?")) {
            statement.setString(1, fundId);
            statement.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // Use database format
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                purchased = resultSet.getInt("count") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchased;
    }

    // Method to check if the mutual fund was purchased on or before a given date
    private boolean isMutualFundPurchasedOnOrBefore(String fundId, LocalDate date) {
        boolean purchased = false;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT COUNT(*) AS count FROM mutual_funds WHERE scheme_code = ? AND mutual_funds.investment_date <= ?")) {
            statement.setString(1, fundId);
            statement.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))); // Use database format
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                purchased = resultSet.getInt("count") > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return purchased;
    }

    // Get the invested amount for a given fund ID from SIPs
    private double getInvestedAmountForFund(String fundId) {
        double amount = 0.0;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(sip_amount) AS total FROM sip WHERE fund_id = ?")) {
            statement.setString(1, fundId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                amount = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }
    private double getInvestedAmount(String fundId) {
        double amount = 0.0;
        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(amount_invested) AS total FROM mutual_funds WHERE scheme_code = ?")) {
            statement.setString(1, fundId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                amount = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return amount;
    }
    private double getTotalValueForFund(String fundId) {

        double totalUnits = 0.0;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(units) AS total FROM mutual_funds WHERE scheme_code = ?")) {
            statement.setString(1, fundId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                totalUnits = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalUnits;
    }

    // Get the total units invested for a given fund ID from SIPs
    private double getTotalUnitsInvestedForFund(String fundId) {
        double totalUnits = 0.0;

        try (Connection connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/javafxapp", "root", "Servesh#21");
             PreparedStatement statement = connection.prepareStatement("SELECT SUM(sip.total_units) AS total FROM sip WHERE fund_id = ?")) {
            statement.setString(1, fundId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                totalUnits = resultSet.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalUnits;
    }

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
