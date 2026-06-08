package controllers;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PrivacyPolicyController {

    @FXML
    private Button btnClosePrivacy;

    public void setCloseAction(EventHandler<javafx.event.ActionEvent> handler) {
        btnClosePrivacy.setOnAction(handler);
    }
}
