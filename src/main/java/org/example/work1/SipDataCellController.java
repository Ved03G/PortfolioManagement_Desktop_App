package org.example.work1;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

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



    // Initialize the controller (Optional, if needed)
private SipData sipData;

    // Set the SIP data to the cell
    public void setSipData(SipData sipData) {
        if (sipData != null) {
            sipNameLabel.setText(sipData.getSipName());
            frequencyLabel.setText(sipData.getFrequency());
            periodLabel.setText(sipData.getPeriod()); // Calculated period
            investedAmountLabel.setText(String.format("₹%.2f", sipData.getInvestedAmount()));
            totalUnitsLabel.setText(String.format("%.2f", sipData.getTotalUnits()));
            currentAmountLabel.setText(String.format("₹%.2f", sipData.getCurrentAmount()));
            returnsLabel.setText(String.format("₹%.2f", sipData.getReturns()));
        }
    }
}
