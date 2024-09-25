
package org.example.work1;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.stage.Stage;

public class SIPController {

    @FXML
    private TextField fundSearchField;
    @FXML
    private ListView<MutualFund> fundListView;
    @FXML
    private TextField sipAmountField;
    @FXML
    private ChoiceBox<String> frequencyChoiceBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;
    @FXML
    private Label navLabel;
    @FXML
    private Label totalUnitsLabel;

    private ObservableList<MutualFund> mutualFunds = FXCollections.observableArrayList();
    private FilteredList<MutualFund> filteredMutualFunds;

    @FXML
    public void initialize() {
        loadMutualFunds();
        setupSearchFilter();
        loadFrequencyOptions();
    }

    // Load mutual funds into the ObservableList
    private void loadMutualFunds() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.mfapi.in/mf"))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            for (JsonNode fundNode : rootNode) {
                String fundId = fundNode.path("schemeCode").asText();
                String schemeName = fundNode.path("schemeName").asText();
                String fundType = fundNode.path("fundType").asText();
                String category = fundNode.path("category").asText();
                mutualFunds.add(new MutualFund(fundId, schemeName, fundType, category, ""));
            }

            filteredMutualFunds = new FilteredList<>(mutualFunds, p -> true);
            fundListView.setItems(filteredMutualFunds);

            fundListView.setVisible(false); // Initially hide ListView

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading mutual funds: " + e.getMessage());
        }
    }

    // Set up search filter for mutual funds
    private void setupSearchFilter() {
        fundSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                filteredMutualFunds.setPredicate(fund -> false); // Hide ListView if search is empty
                fundListView.setVisible(false);
            } else {
                filteredMutualFunds.setPredicate(fund -> fund.getSchemeName().toLowerCase().contains(newValue.toLowerCase()));
                fundListView.setVisible(!filteredMutualFunds.isEmpty()); // Show ListView if there are results
            }
        });

        // Handle selection from ListView
        fundListView.setOnMouseClicked((MouseEvent event) -> {
            MutualFund selectedFund = fundListView.getSelectionModel().getSelectedItem();
            if (selectedFund != null) {
                fundSearchField.setText(selectedFund.getSchemeName());
                fundListView.setVisible(false); // Hide ListView after selection
            }
        });
    }

    // Load frequency options for SIP
    private void loadFrequencyOptions() {
        ObservableList<String> frequencyOptions = FXCollections.observableArrayList("Monthly", "Quarterly", "Yearly");
        frequencyChoiceBox.setItems(frequencyOptions);
    }

    // Handle SIP investment
    public void handleSIPInvestment() {
        String selectedFundName = fundSearchField.getText();
        MutualFund selectedFund = findFundByName(selectedFundName);

        if (selectedFund == null) {
            System.out.println("No valid fund selected.");
            navLabel.setText("No valid fund selected.");
            return;
        }

        String sipAmountText = sipAmountField.getText();
        if (sipAmountText == null || sipAmountText.isEmpty()) {
            System.out.println("SIP amount is empty.");
            navLabel.setText("Please enter SIP amount.");
            return;
        }

        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String frequency = frequencyChoiceBox.getValue();

        if (startDate == null || endDate == null || frequency == null) {
            System.out.println("Start date, end date, or frequency is not selected.");
            navLabel.setText("Select all SIP details.");
            return;
        }

        double totalUnits = 0;
        double sipAmount = 0;
        try {
            sipAmount = Double.parseDouble(sipAmountText);
            String fundId = selectedFund.getsipid();

            String fundDataUrl = "https://api.mfapi.in/mf/" + fundId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(fundDataUrl))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            MutualFund fundWithNav = parseFundDetails(jsonResponse); // Static method call
            if (fundWithNav == null || fundWithNav.getNav() == null) {
                System.out.println("Failed to fetch NAV.");
                navLabel.setText("Failed to fetch NAV.");
                return;
            }

            double nav = Double.parseDouble(fundWithNav.getNav());
            totalUnits = sipAmount / nav;

            navLabel.setText(String.format("NAV: %.2f", nav));
            totalUnitsLabel.setText(String.format("Total Units: %.2f", totalUnits));

            MutualFund fund = parseFundDetails(jsonResponse);
            String schemeName = fund.getSchemeName();

            storeSIPDetails(selectedFund, sipAmount, totalUnits, startDate, endDate, frequency, schemeName);
            storetransactiondata(sipAmount, totalUnits, schemeName);


        } catch (NumberFormatException e) {
            System.out.println("Invalid SIP amount entered.");
            navLabel.setText("Invalid SIP amount.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.out.println("Error fetching NAV data: " + e.getMessage());
            navLabel.setText("Error fetching NAV.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error during SIP investment: " + e.getMessage());
            navLabel.setText("Error during SIP investment.");
        }


        try {
            reloadSIPManagementScreen();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        double finalTotalUnits = totalUnits;
        double finalSipAmount = sipAmount;
        Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Buy Data");
                    alert.setHeaderText(null);
                    alert.setContentText("Successfully bought " + finalTotalUnits + "units of" + selectedFund.schemeName + "for ₹" + finalSipAmount);
                    alert.showAndWait();
                });


//        stage.close();

    }
    private void reloadSIPManagementScreen() throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage) sipAmountField.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }
    }

    private MutualFund parseFundDetails(String jsonResponse) {
        JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
        JsonObject metaObject = jsonObject.getAsJsonObject("meta");
        String schemeName = metaObject.has("scheme_name") ? metaObject.get("scheme_name").getAsString() : "Unknown Scheme";
        String fundId = metaObject.has("scheme_code") ? metaObject.get("scheme_code").getAsString() : "Unknown Scheme";
        String fundType = metaObject.has("scheme_type") ? metaObject.get("scheme_type").getAsString() : "Unknown Scheme";
        String category = metaObject.has("scheme_category") ? metaObject.get("scheme_category").getAsString() : "Unknown Scheme";
        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        String nav = "Unknown NAV";
        if (dataArray.size() > 0) {
            JsonObject dataObject = dataArray.get(0).getAsJsonObject();
            nav = dataObject.has("nav") ? dataObject.get("nav").getAsString() : "Unknown NAV";
        }
        return new MutualFund(fundId,schemeName,fundType,category,nav);
    }



    // Helper method to find MutualFund by name
    private MutualFund findFundByName(String schemeName) {
        for (MutualFund fund : mutualFunds) {
            if (fund.getSchemeName().equals(schemeName)) {
                return fund;
            }
        }
        return null;
    }

    public void backtosipManagement(ActionEvent actionEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage) sipAmountField.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }

    }

    public class MutualFund {
        private String sipid;
        private String schemeName;
        private String fundType;
        private String category;
        private String nav;

        public MutualFund(String sipid, String schemeName, String fundType, String category, String nav) {
            this.sipid = sipid;
            this.schemeName = schemeName;
            this.fundType = fundType;
            this.category = category;
            this.nav = nav;
        }



        // Getters
        public String getsipid() {
            return sipid;
        }

        public String getSchemeName() {
            return schemeName;
        }

        public String getNav() {
            return nav;
        }

        // Override toString() to display fund name in ComboBox
        @Override
        public String toString() {
            return schemeName;
        }

    }
    private void storeSIPDetails(MutualFund fund, double sipAmount, double totalUnits, LocalDate startDate, LocalDate endDate, String frequency, String fund_Name) {
        String URL = "jdbc:mysql://localhost:3306/javafxapp" ; // Update as necessary
        String USER = "root"; // Your MySQL username
        String PASSWORD = "Servesh#21"; // Your MySQL password
        String insertSQL = "INSERT INTO sip (fund_id,sip_amount, total_units, start_date, end_date, frequency,fund_Name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            stmt.setString(1, fund.getsipid());
            stmt.setDouble(2, sipAmount);
            stmt.setDouble(3, totalUnits);
            stmt.setDate(4, java.sql.Date.valueOf(startDate));
            stmt.setDate(5, java.sql.Date.valueOf(endDate));
            stmt.setString(6, frequency);
            stmt.setString(7, fund_Name);

            stmt.executeUpdate();
            System.out.println("SIP data successfully stored in the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error storing SIP data: " + e.getMessage());
        }
    }
    private void storetransactiondata(double sipAmount,double totalUnits,String schemename) {
        String URL = "jdbc:mysql://127.0.0.1:3306/javafxapp" ; // Update as necessary
        String USER = "root"; // Your MySQL username
        String PASSWORD = "Servesh#21"; // Your MySQL password

        String insertSQL = "INSERT INTO transactions ( Amount, units, type1, transaction_date, fund_name,fund_type) VALUES ( ?, ?, ?, ?, ?,?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {


            stmt.setDouble(1, sipAmount);
            stmt.setDouble(2, totalUnits);
            stmt.setString(3, "Buy");
            stmt.setString(4, String.valueOf(java.sql.Date.valueOf(LocalDate.now())));
            stmt.setString(5,schemename);
            stmt.setString(6, "SIP");

            stmt.executeUpdate();
            System.out.println("SIP data successfully stored in the database.");

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error storing SIP data: " + e.getMessage());
        }
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
