package org.example.work1;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.example.work1.TransactionCellController;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Transactionview {
    private Stage stage;
    private Scene scene;
    private Parent root;
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/javafxapp" ; // Update as necessary
    private static final String USER = "root"; // Your MySQL username
    private static final String PASSWORD = "Vedant@98"; // Your MySQL password
    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    @FXML
    private    ListView<Transaction> transactionListView;
    @FXML
    private Button btnPortfolio,btnSIP,btnMutualFunds,btnReports,btnTransactions,btnProfile;

    // Method to load the FXML of each section
    private void switchToPage1(ActionEvent event, String fxmlFile1, String title, Button clickedButton) throws IOException {
        FXMLLoader loader1 = new FXMLLoader(getClass().getResource(fxmlFile1));
        root = loader1.load();
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle(title);
        stage.show();
        resetButtonStyles();
        clickedButton.getStyleClass().add("sidebar-button-active");
    }
    private void resetButtonStyles() {
        List<Button> buttons = List.of(btnPortfolio, btnSIP, btnMutualFunds, btnReports, btnTransactions, btnProfile);
        for (Button button : buttons) {
            button.getStyleClass().remove("sidebar-button-active");
        }
    }

    public void handlePortfolioButtonClick(ActionEvent event) throws IOException {
        switchToPage1(event, "PortfolioManagement.fxml", "Portfolio Management", btnPortfolio);
    }

    public void handlesipclick(ActionEvent event) throws IOException {
        switchToPage1(event, "SIPManagement.fxml", "SIP Management",btnSIP);
    }

    public void handlemutualfundclick(ActionEvent event) throws IOException {
        switchToPage1(event, "MutualFunds.fxml", "Mutual Funds",btnMutualFunds);
    }

    public void handlereportsclick(ActionEvent event) throws IOException {
        switchToPage1(event, "ReportsAnalytics.fxml", "Reports & Analytics",btnReports);
    }

    public void handletransactionclick(ActionEvent event) throws IOException {
        switchToPage1(event, "TransactionHistory.fxml", "Transaction History",btnTransactions);
    }

    public void handleprofileclick(ActionEvent event) throws IOException {
        switchToPage1(event, "UserProfile.fxml", "User Profile",btnProfile);
    }
    // Method to load transactions from the database
    public void loadTransactionsFromDatabase() {
        String query = "SELECT Amount,type1, transaction_date, fund_type,fund_name,units FROM transactions";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String amount = String.valueOf(rs.getDouble("Amount"));
                String type = rs.getString("type1");
                Date transactionDate;
                transactionDate = rs.getDate("transaction_date");
                String fundtype = rs.getString("fund_type");
                String fundname = rs.getString("fund_name");
                double units = rs.getDouble("units");


                // Add the transaction to the list
                Transaction transaction = new Transaction(amount,type,transactionDate,fundtype,fundname,units);
                transactionList.add(transaction);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Set the items in the ListView
        transactionListView.setItems(transactionList);
    }
    @FXML
    private void initialize() {

        transactionListView.setCellFactory(listView -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction transaction, boolean empty) {
                super.updateItem(transaction, empty);
                if (empty || transaction == null) {
                    setGraphic(null);
                } else {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("transactioncell.fxml"));
                    try {
                        AnchorPane cell = loader.load();
                        TransactionCellController controller = loader.getController();
                        controller.setTransactionData(transaction);
                        setGraphic(cell);
                        setPadding(new Insets(10));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // Load transactions from the database and display them in the ListView
        loadTransactionsFromDatabase();
    }

}
