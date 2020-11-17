package ui.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ControllerUpdate {


    @FXML
    private Label updateFinishTitle;

    @FXML
    private Label updateNowTitle;

    @FXML
    private Label updateNotNewVersionTitle;

    @FXML
    private Button updateLate;

    @FXML
    private Button updateNow;

    @FXML
    private Button updateOk;

    private Stage currentStage;

    public static void showUpdate(Stage mainStage, boolean isNewVersion) throws IOException {
        Stage newStage = new Stage();
        newStage.getIcons().add(new Image("ic_logo.png"));
        FXMLLoader fxmlLoader = BaseController.showScene(newStage, "layout_update_dialog.fxml", "Update Dialog");
        ControllerUpdate controllerTestConnect = fxmlLoader.getController();
        controllerTestConnect.initView(newStage, mainStage, isNewVersion);
        newStage.show();
    }

    private void initView(Stage currentStage, Stage mainStage, boolean isNewVersion) {
        this.currentStage = currentStage;
        if (isNewVersion) {
            updateOk.setVisible(false);
            updateNotNewVersionTitle.setVisible(false);
            updateLate.setOnMouseClicked(event ->
                    this.currentStage.close()
            );
            updateNow.setOnMouseClicked(event -> {
                this.currentStage.close();
                mainStage.close();
                System.exit(0);
            });
        } else {
            updateLate.setVisible(false);
            updateNow.setVisible(false);
            updateFinishTitle.setVisible(false);
            updateNowTitle.setVisible(false);
            updateOk.setOnMouseClicked(event ->
                    this.currentStage.close()
            );
        }
    }

}
