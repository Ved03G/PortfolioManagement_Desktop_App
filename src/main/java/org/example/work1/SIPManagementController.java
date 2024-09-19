package org.example.work1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
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

    private ObservableList<SipData> sipDataList = FXCollections.observableArrayList();

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
    private void initialize() {
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
                    }
                };
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        });
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
        String query = "SELECT fund_id FROM sip WHERE fund_name = ?";
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
