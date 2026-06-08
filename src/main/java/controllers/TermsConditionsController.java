package controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class TermsConditionsController {

    @FXML
    private Button btnCloseTerms;

    public void setCloseAction(EventHandler<javafx.event.ActionEvent> handler) {
        btnCloseTerms.setOnAction(handler);
    }
}
