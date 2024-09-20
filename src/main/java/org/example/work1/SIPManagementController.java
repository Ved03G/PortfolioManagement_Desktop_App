package org.example.work1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class SIPManagementController {

    @FXML
    public ListView<SipData> sipListView;
    @FXML
    public TextField searchField;
    @FXML
    public Button btnAddSIP;
    @FXML
    public Button btnSellSIP;
    private Stage stage;
    private Scene scene;
    private Parent root;
    @FXML
    private Button btnPortfolio,btnSIP,btnMutualFunds,btnReports,btnTransactions,btnProfile;
    @FXML
    private Label navLabel;

    @FXML
    private ObservableList<SipData> sipDataList = FXCollections.observableArrayList();
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/mutualfundsdb";
//    private static final String DB_USER = "root";
//    private static final String DB_PASSWORD = "Servesh#21";
    // Method to load the FXML of each section
    private void switchToPage2(ActionEvent event, String fxmlFile, String title,Button clickedButton) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        root = loader.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        resetButtonStyles();
        clickedButton.getStyleClass().add("sidebar-button-active");
    }
    private void resetButtonStyles() {
        java.util.List<Button> buttons = List.of(btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile);
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage2(event, "PortfolioManagement.fxml", "Portfolio Management", btnPortfolio);
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage2(event, "SIPManagement.fxml", "SIP Management",btnSIP);
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage2(event, "MutualFunds.fxml", "Mutual Funds",btnMutualFunds);
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage2(event, "ReportsAnalytics.fxml", "Reports & Analytics",btnReports);
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage2(event, "TransactionHistory.fxml", "Transaction History",btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage2(event, "UserProfile.fxml", "User Profile",btnProfile);
    }
    public void handleStartsipclick(ActionEvent event) throws IOException {
        // Load the addsip.fxml file
        Parent root = FXMLLoader.load(getClass().getResource("addsip.fxml"));

        // Create a new Stage for the "Start SIP" window
        Stage sipStage = new Stage();
        sipStage.setTitle("Start New SIP");

        // Set the scene for the new stage
        Scene sipScene = new Scene(root);
        sipStage.setScene(sipScene);

        // Show the new stage (window)
        sipStage.show();
        closesipmanagement();

    }
    public void handlesellclick(ActionEvent event) throws IOException {
        // Load the sellsip.fxml file
        Parent root = FXMLLoader.load(getClass().getResource("sellsip.fxml"));

        // Create a new Stage for the "Sell SIP" window
        Stage sellStage = new Stage();
        sellStage.setTitle("Sell SIP");

        // Set the scene for the new stage
        Scene sellScene = new Scene(root);
        sellStage.setScene(sellScene);

        // Show the new stage (window)
        sellStage.show();
        closesipmanagement();

    }

    private void closesipmanagement() {
        Stage stage = (Stage) sipListView.getScene().getWindow();
        stage.close();
    }


    @FXML
    private void initialize() {
        // Set cell factory
        sipListView.setCellFactory(listView -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("sipdatacell.fxml"));
                AnchorPane cell = loader.load();
                SipDataCellController controller = loader.getController();
                return new ListCell<SipData>() {
                    @Override
                    protected void updateItem(SipData item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setGraphic(null);
                        } else {
                            controller.setSipData(item);
                            setGraphic(cell);
                        }
                        setPadding(new Insets(10));
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
        // Load SIP data from the database
        loadSipData();

        // Set up search field listener
        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterSipData(newValue));
    }
    private int getCurrentUserId() {
        return UserSession.getInstance().getUserId();
    }
    // Load SIP data from the database
    private void loadSipData() {
        String query = "SELECT fund_Name, frequency, start_date, end_date, sip_amount, total_units FROM sip";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String sipName = rs.getString("fund_Name");
                String frequency = rs.getString("frequency");
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                double investedAmount = rs.getDouble("sip_amount");
                double totalUnits = rs.getDouble("total_units");
                double currentAmount = fetchCurrentAmount(sipName, totalUnits);
                double returns = currentAmount - investedAmount;

                sipDataList.add(new SipData(sipName, frequency, startDate, endDate, investedAmount, totalUnits, currentAmount, returns));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        sipListView.setItems(sipDataList);
    }

    // Fetch the current amount based on total units and current NAV
    private double fetchCurrentAmount(String sipName, double totalUnits) {
        double nav = getCurrentNavForFund(sipName); // Assuming sipName is used as fundId for NAV lookup
        return totalUnits * nav;
    }

    // Fetch the current NAV for the SIP (This method should call your API or database)
    private double fetchNavForFund(String fundId) {
        String navApiUrl = "https://api.mfapi.in/mf/" + fundId;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(navApiUrl))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String jsonResponse = response.body();

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode navNode = rootNode.path("data").get(0).path("nav");

            return navNode.asDouble();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return 0.0;
    }

    // Fetch fund ID from fund name
    private String getFundIdFromName(String fundName) {
        String query = "SELECT fund_id FROM sip WHERE fund_Name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, fundName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("fund_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private double getCurrentNavForFund(String fundName) {
        String fundId = getFundIdFromName(fundName);
        if (fundId == null) {
            return 0.0;
        }
        return fetchNavForFund(fundId);
    }

    // Filter SIP data based on search input
    private void filterSipData(String searchText) {
        ObservableList<SipData> filteredList = FXCollections.observableArrayList();
        for (SipData data : sipListView.getItems()) {
            if (data.getSipName().toLowerCase().contains(searchText.toLowerCase())) {
                filteredList.add(data);
            }
        }
        sipListView.setItems(filteredList);
    }



}
