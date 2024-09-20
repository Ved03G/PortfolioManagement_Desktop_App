package org.example.work1.work1;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class TransactionCellController {


    @FXML
    private FontAwesomeIconView fwd;
    @FXML
    private FontAwesomeIconView bwd;

    @FXML
    private Label date_lbl,amount_lbl,type_lbl,description_lbl,name_lbl;




    public void setTransactionData(Transaction transaction){
        if(transaction != null){
            date_lbl.setText(transaction.getDate().toString());
            amount_lbl.setText(transaction.getAmount());
            type_lbl.setText(transaction.getFundtype());
            name_lbl.setText(transaction.getFundname());
            if(transaction.getType().equals("Buy")){
                fwd.setVisible(true);
                bwd.setVisible(false);
            }else{
                fwd.setVisible(false);
                bwd.setVisible(true);
            }

        }

    }


}
