package org.example.work1.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ClientDashboardController {

    private Stage stage;
    private Scene scene;
    private Parent root;

    // Add references to your buttons
    public Button btnPortfolio;
    public Button btnSIP;
    public Button btnMutualFunds;
    public Button btnReports;
    public Button btnTransactions;
    public Button btnProfile;

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
