package org.example.work1;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SipDataCellController {

    @FXML
    private Label sipNameLabel;
    @FXML
    private Label frequencyLabel;
    @FXML
    private Label periodLabel;
    @FXML
    private Label investedAmountLabel;
    @FXML
    private Label totalUnitsLabel;
    @FXML
    private Label currentAmountLabel;
    @FXML
    private Label returnsLabel;
    @FXML
    private SipData sipData;



    // Initialize the controller (Optional, if needed)
// Set the SIP data to the cell
    public void setSipData(SipData sipData) {
        this.sipData = sipData;
        if (sipData != null) {
            sipNameLabel.setText(sipData.getSipName());
            frequencyLabel.setText(sipData.getFrequency());
            periodLabel.setText(sipData.getPeriod()); // Calculated period
            investedAmountLabel.setText(String.format("₹%.2f", sipData.getInvestedAmount()));
            totalUnitsLabel.setText(String.format("%.2f", sipData.getTotalUnits()));

            // Current amount and returns
            double currentAmount = sipData.getCurrentAmount();
            double investedAmount = sipData.getInvestedAmount();
            double returns = sipData.getReturns();

            currentAmountLabel.setText(String.format("₹%.2f", currentAmount));
            returnsLabel.setText(String.format("₹%.2f", returns));

            // Set color based on profit or loss
            if (currentAmount > investedAmount) {
                currentAmountLabel.setStyle("-fx-text-fill: green;"); // Profit
                returnsLabel.setStyle("-fx-text-fill: green;"); // Profit
            } else if (currentAmount < investedAmount) {
                currentAmountLabel.setStyle("-fx-text-fill: red;"); // Loss
                returnsLabel.setStyle("-fx-text-fill: red;"); // Loss
            }
            else{
                currentAmountLabel.setStyle("-fx-text-fill: #a5a3a3;");
                returnsLabel.setStyle("-fx-text-fill: #a5a3a3;");
            }
        }
    }
    @FXML
    private Button handlesell; // This should be defined in your FXML



    public void handlesell(ActionEvent actionEvent) {

        System.out.println("handleSell method called");

            if (sipData == null) {
                showAlert("Error", "No SIP Data", "No SIP data available for selling.");
                return; // Exit if no data
            }


        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sell Fund");
        dialog.setHeaderText("Sell Fund: " + sipData.getSipName());
        dialog.setContentText("Total Units Available: " + sipData.getTotalUnits() +
                "\nEnter the Units You Want To Sell:");

        dialog.showAndWait().ifPresent(unitStr -> {
            try {
                double unitsToSell = Double.parseDouble(unitStr);
                double availableUnits = sipData.getTotalUnits();
                double investedAmount = unitsToSell* sipData.getNav();

                // Check for sufficient units
                if (unitsToSell > availableUnits) {
                    showAlert("Error", "Invalid Input", "You Do Not Have Sufficient Units!");
                    return;
                }

                double remainingUnits = availableUnits - unitsToSell;
                double nav = sipData.getNav(); // Assuming SIP data has NAV
                double amountInvested = remainingUnits * nav;


                // Store transaction and update database
                deleteFundFromDatabase(amountInvested, remainingUnits, sipData.getSip_id());
               // saveToTransactions(amountInvested, unitsToSell, sipData.getSipName(), "Sell");

                // Success alert
                showAlert("Success", "Success", "You have successfully sold " + unitsToSell +
                        " units for ₹ " + investedAmount +
                        ". Remaining Units: " + remainingUnits +
                        ", Remaining Amount: " + amountInvested);
                reloadSIPManagementScreen();
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid Input", "Please enter a valid number for the amount.");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void reloadSIPManagementScreen() throws IOException {
        // Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("SIPManagement.fxml"));

        Parent newRoot = loader.load();
        // Get the current stage
        Stage currentStage = (Stage) investedAmountLabel.getScene().getWindow();
        if (currentStage != null) {
            // Create a new Scene and set it to the current stage
            currentStage.setScene(new Scene(newRoot));
            currentStage.show(); // Make sure to show the stage
        } else {
            System.out.println("Current stage is null");
        }
    }
    // Utility method for showing alerts
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void deleteFundFromDatabase(double amount, double units,int sip_id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            String deleteQuery = "UPDATE sip Set total_units=?,sip_amount=? where sip_id=?";
            PreparedStatement ps = connection.prepareStatement(deleteQuery);

            ps.setDouble(1, units);// Set the scheme code of the selected fund
            ps.setDouble(2, amount);
            ps.setInt(3, sip_id);
            ps.executeUpdate();  // Execute the deletion

            ps.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        String deleteQuery = "DELETE FROM sip WHERE sip.total_units=?";
        try(Connection conn=DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, 0);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
