package org.example.work1;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
public class MutualFundsController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    // Sidebar buttons
    @FXML
    private TableView<MutualFund2> investmentTable;
    @FXML
    private TextField searchSchemeCode1, searchSchemeName1;
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

    private ObservableList<MutualFund2> mutualFundsList = FXCollections.observableArrayList();

    @FXML
    private Button btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile;

    // Table and search fields
    @FXML
    private TextField searchSchemeCode, searchSchemeName;

    @FXML
    private TableView<MutualFund> mutualFundTable;

    @FXML
    private TableColumn<MutualFund, String> columnSchemeCode, columnSchemeName;

    private ObservableList<MutualFund> mutualFundData = FXCollections.observableArrayList();
    private MutualFund selectedFund;
    private double currentNav = 0.0;
    private double costperunit = 0.0;

    private void calculateCurrentValues() {
        for (MutualFund2 fund : mutualFundsList) {
            double currentValue = fund.getNav() * fund.getUnits();
            fund.setCurrentValue(currentValue); // Assuming you have a setter for currentValue
        }
    }
    // Initialize method to load data into the table and handle sidebar
    @FXML
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



        columnSchemeCode.setCellValueFactory(new PropertyValueFactory<>("schemeCode"));
        columnSchemeName.setCellValueFactory(new PropertyValueFactory<>("schemeName"));

        loadMutualFunds();  // Load data when initializing
        calculateCurrentValues();
        // Handle table row selection
        mutualFundTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFund = newSelection;
            }
        });
        resetButtonStyles();
        btnMutualFunds.getStyleClass().add("sidebar-button-active");
    }
    public void loadTableData() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();
            int userId = getCurrentUserId(); // Get the current user ID

            String query = "SELECT scheme_code, fund_name, nav, amount_invested, current_value, units, costperunit FROM mutual_funds WHERE user_id = " + userId;
            ResultSet resultSet = statement.executeQuery(query);

            mutualFundsList.clear(); // Clear the list before loading new data

            while (resultSet.next()) {
                String schemeCode = resultSet.getString("scheme_code");
                String schemeName = resultSet.getString("fund_name");
                double nav = resultSet.getDouble("nav");
                double amountInvested = resultSet.getDouble("amount_invested");
                double currentValue = resultSet.getDouble("current_value");
                double units = resultSet.getDouble("units");
                double costPerUnit = resultSet.getDouble("costperunit");

                MutualFund2 mutualFund = new MutualFund2(schemeCode, schemeName, nav, amountInvested, currentValue, units, costPerUnit);
                mutualFundsList.add(mutualFund);
            }

            investmentTable.setItems(mutualFundsList);

            resultSet.close();
            statement.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Fetch mutual funds from API and populate the table
    private void loadMutualFunds() {
        MutualFundFetcher fetcher = new MutualFundFetcher();
        JSONArray mutualFunds = fetcher.fetchData();

        if (mutualFunds != null) {
            for (Object obj : mutualFunds) {
                JSONObject fund = (JSONObject) obj;

                // Handle schemeCode conversion from Long to String
                Object schemeCodeObj = fund.get("schemeCode");
                String schemeCode = schemeCodeObj instanceof Long ? String.valueOf(schemeCodeObj) : (String) schemeCodeObj;

                mutualFundData.add(new MutualFund(
                        schemeCode,
                        (String) fund.get("schemeName")
                ));
            }
        }

        mutualFundTable.setItems(mutualFundData);
    }


    // Filter table data based on search input
    @FXML
    public void onSearch() {
        String schemeCodeFilter = searchSchemeCode.getText().toLowerCase();
        String schemeNameFilter = searchSchemeName.getText().toLowerCase();

        ObservableList<MutualFund> filteredData = FXCollections.observableArrayList();

        for (MutualFund fund : mutualFundData) {
            if (fund.getSchemeCode().toLowerCase().contains(schemeCodeFilter)
                    && fund.getSchemeName().toLowerCase().contains(schemeNameFilter)) {
                filteredData.add(fund);
            }
        }

        mutualFundTable.setItems(filteredData);
    }
    @FXML
    public void onInvestmentSearch() {
        String schemeCodeFilter = searchSchemeCode1.getText().toLowerCase();
        String schemeNameFilter = searchSchemeName1.getText().toLowerCase();

        ObservableList<MutualFund2> filteredData = FXCollections.observableArrayList();

        for (MutualFund2 fund : mutualFundsList) {
            if (fund.getSchemeCode().toLowerCase().contains(schemeCodeFilter)
                    && fund.getSchemeName().toLowerCase().contains(schemeNameFilter)) {
                filteredData.add(fund);
            }
        }

        investmentTable.setItems(filteredData);
    }
    private void reloadSMFManagementScreen() throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MutualFunds.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage)searchSchemeCode.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }
    }

    @FXML
    public void fetchNAVData() {
        if (selectedFund != null) {
            MutualFundFetcher fetcher = new MutualFundFetcher();
            JSONObject fundData = fetcher.fetchFundDataBySchemeCode(selectedFund.getSchemeCode());

            if (fundData != null) {
                JSONObject data = (JSONObject) ((JSONArray) fundData.get("data")).get(0);
                String date = (String) data.get("date");
                currentNav = Double.parseDouble((String) data.get("nav")); // Save NAV for later calculations
                costperunit = currentNav;
                // Show the fetched NAV and date to the user (e.g., in an alert)
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("NAV Data");
                alert.setHeaderText("NAV Information");
                alert.setContentText("Date: " + date + "\nNAV: " + currentNav);
                alert.showAndWait();
            }
        } else {
            // Show error if no fund is selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Mutual Fund Selected");
            alert.setContentText("Please select a mutual fund first.");
            alert.showAndWait();
        }
    }
    // Add selected mutual fund to investment
    @FXML
    public void addToInvestment() throws IOException {
        if (selectedFund != null) {
            if (currentNav == 0.0) {
                // Ensure that NAV is fetched before allowing investment
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Warning");
                alert.setHeaderText("NAV Not Fetched");
                alert.setContentText("Please fetch the NAV data before making an investment.");
                alert.showAndWait();
                return;
            }

            // Show input dialog to ask how much the user wants to invest
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Investment Amount");
            dialog.setHeaderText("Enter the amount you want to invest:");
            dialog.setContentText("Amount:");

            // Get user input for investment amount
            dialog.showAndWait().ifPresent(amountStr -> {
                try {
                    double amount = Double.parseDouble(amountStr);
                    double units = amount / currentNav; // Calculate units based on NAV
                    units = Math.round(units * 100.0) / 100.0; // Round to 2 decimal places
                    double currentValue = units * currentNav;
                    // Save investment details to the database
                    saveInvestmentToDatabase(amount, units, currentValue,costperunit);
                    savetotransactions( amount, units,selectedFund.getSchemeName(),"Buy");
                    // Show investment information
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Investment Successful");
                    alert.setHeaderText("Investment Details");
                    alert.setContentText(
                            "You have invested Rs. " + amount + " in " + selectedFund.getSchemeName() + ".\n\n" +
                                    "You will receive " + units + " units based on the current NAV of " + currentNav + "."
                    );

                    alert.showAndWait();
                } catch (NumberFormatException e) {
                    // Handle invalid input
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number for the amount.");
                    alert.showAndWait();
                }
            });
        } else {
            // Show error if no fund is selected
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No Mutual Fund Selected");
            alert.setContentText("Please select a mutual fund first.");
            alert.showAndWait();
        }
        reloadSMFManagementScreen();
    }
    // Function to save investment details to the database
    private void saveInvestmentToDatabase(double amountInvested, double units, double currentValue, double costperunit) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            String insertQuery = "INSERT INTO mutual_funds (user_id, fund_name, amount_invested, current_value, investment_date, scheme_code, nav, units, costperunit) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(insertQuery);

            int userId = getCurrentUserId();
            updatePortfolio(userId);
            ps.setInt(1, userId);
            ps.setString(2, selectedFund.getSchemeName());
            ps.setDouble(3, amountInvested);
            ps.setDouble(4, currentValue);
            ps.setTimestamp(5, new Timestamp(new Date().getTime()));
            ps.setString(6, selectedFund.getSchemeCode());
            ps.setDouble(7, currentNav);
            ps.setDouble(8, units);
            ps.setDouble(9, costperunit);

            ps.executeUpdate();
            ps.close();
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    private void savetotransactions(double AmountInvested, double units, String Name,String type){
        String insertSQL = "INSERT INTO transactions ( Amount, units, type1, transaction_date, fund_name,fund_type) VALUES ( ?, ?, ?, ?, ?,?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {


            stmt.setDouble(1, AmountInvested);
            stmt.setDouble(2, units);
            stmt.setString(3, type);
            stmt.setString(4, String.valueOf(java.sql.Date.valueOf(LocalDate.now())));
            stmt.setString(5, Name);
            stmt.setString(6, "Mutual Fund");

            stmt.executeUpdate();
            System.out.println("SIP data successfully stored in the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error storing SIP data: " + e.getMessage());
        }
    }



    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    public void updatePortfolio(int userId) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String sumQuery = "SELECT SUM(amount_invested) AS totalAmountInvested, SUM(current_value) AS totalCurrentValue " +
                    "FROM mutual_funds WHERE user_id = ?";
            PreparedStatement ps = connection.prepareStatement(sumQuery);
            ps.setInt(1, userId);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                double totalAmountInvested = resultSet.getDouble("totalAmountInvested");
                double totalCurrentValue = resultSet.getDouble("totalCurrentValue");

                String insertQuery = "INSERT INTO portfolio (user_id, amount_invested, current_value,type) VALUES (?, ?, ?,?)";
                PreparedStatement insertPs = connection.prepareStatement(insertQuery);
                insertPs.setInt(1, userId);
                insertPs.setDouble(2, totalAmountInvested);
                insertPs.setDouble(3, totalCurrentValue);
                insertPs.setString(4, "Mutual Fund");
                insertPs.executeUpdate();
                insertPs.close();
            }

            resultSet.close();
            ps.close();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Method to handle the selling of a fund
    @FXML
    public void handleSellFund(ActionEvent event) throws IOException {
        MutualFund2 selectedfund = investmentTable.getSelectionModel().getSelectedItem();

        if (selectedfund != null) {
            // Confirm with the user before deleting
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Sell Fund");
            dialog.setHeaderText("Sell Fund: " + selectedfund.getSchemeName());
            dialog.setHeaderText("Total Units Available: " + selectedfund.getUnits());
            dialog.setContentText("Enter the Units You Want To sell");

            dialog.showAndWait().ifPresent( unitstr-> {
                try {
                    double units = Double.parseDouble(unitstr);
                    double amount=units*selectedfund.getNav();
                    if (units > selectedfund.getUnits()) {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText("Invalid Input");
                        alert.setContentText("You Do Not Havr Sufficient Units!");
                        return;
                    } else {
                       double units1 = selectedfund.getUnits() - units;
                        double nav = selectedfund.getNav();
                        double amountInvested = units1 * nav;
                        deleteFundFromDatabase(amountInvested, units1,nav,selectedfund.getSchemeCode());
                        savetotransactions(amountInvested, units,selectedfund.getSchemeName(),"Sell");
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Success");
                        alert.setHeaderText("Success");
                        alert.setContentText("You have successfully Sold  " +units + " for ₹ " +amount + " Remaning Units"+units1 +",remaining Amount" +amountInvested);
                        alert.showAndWait();
                    }
                }catch(NumberFormatException e){
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number for the amount.");
                    alert.showAndWait();
                }

            });


    }
        reloadSMFManagementScreen();
    }


    // Method to delete the selected fund from the database
    private void deleteFundFromDatabase(double amount, double units,double nav,String schemecode) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String deleteQuery = "UPDATE mutual_funds Set units=?,amount_invested=?,current_value=? where scheme_code=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);

            ps.setDouble(1, units);// Set the scheme code of the selected fund
            ps.setDouble(2, amount);
            ps.setDouble(3, units*nav);
            ps.setString(4, schemecode);
            ps.executeUpdate();  // Execute the deletion

            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String deleteQuery = "DELETE FROM mutual_funds WHERE units=?";
        try(Connection conn=DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, 0);
            stmt.executeUpdate();
    } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // Method to load the FXML of each section
    private void switchToPage(ActionEvent event, String fxmlFile, String title, Button clickedButton) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();

        // Reset button styles and set the clicked button as active
        resetButtonStyles();
        clickedButton.getStyleClass().add("sidebar-button-active");
    }

    // Reset all buttons to default style
    private void resetButtonStyles() {
        List<Button> buttons = List.of(btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile);
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    // Sidebar button handlers
    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage(event, "PortfolioManagement.fxml", "Portfolio Management", btnPortfolio);
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage(event, "SIPManagement.fxml", "SIP Management", btnSIP);
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage(event, "MutualFunds.fxml", "Mutual Funds", btnMutualFunds);
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage(event, "ReportsAnalytics.fxml", "Reports & Analytics", btnReports);
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage(event, "TransactionHistory.fxml", "Transaction History", btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage(event, "UserProfile.fxml", "User Profile", btnProfile);
    }
}
