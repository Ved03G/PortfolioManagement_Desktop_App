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
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.Date;

public class TransactionHistoryController {

    private Stage stage;
    private Scene scene;
    private Parent root;

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


    public class Transactionview {
        private static final String DB_URL = "jdbc:mysql://localhost:3306/mutualfundsdb";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "Servesh#21";
        private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
        @FXML
        private ListView<Transaction> transactionListView;
        // Method to load transactions from the database
        public void loadTransactionsFromDatabase() {
            String query = "SELECT Amount,type, transaction_date, fundtype, units,fund_name FROM transactions";
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                 PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    String amount = String.valueOf(rs.getDouble("Amount"));
                    String type = rs.getString("type");
                    Date transactionDate;
                    transactionDate = rs.getDate("transaction_date");
                    String fundtype = rs.getString("fundtype");
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

}
