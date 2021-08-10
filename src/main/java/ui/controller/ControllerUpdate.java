package ui.controller;

import config.ConfigKey;
import connect.joggle.IUpdateConfirmCallBack;
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

    @FXML
    private Label updateVersion;

    private static Stage currentStage;

    public static void showUpdate(boolean isNewVersion, IUpdateConfirmCallBack callBack) throws IOException {
        Stage newStage = new Stage();
        newStage.setResizable(false);
        newStage.getIcons().add(new Image("res/ic_logo.png"));
        FXMLLoader fxmlLoader = BaseController.showScene(newStage, "res/layout_update_dialog.fxml", "Update Dialog");
        ControllerUpdate controllerTestConnect = fxmlLoader.getController();
        controllerTestConnect.initView(newStage, isNewVersion, callBack);
        newStage.show();
    }

    public static void close() {
        if (currentStage != null) {
            currentStage.close();
            currentStage = null;
        }
    }

    private void initView(Stage currentStage, boolean isNewVersion, IUpdateConfirmCallBack callBack) {
        this.currentStage = currentStage;
        updateVersion.setText(ConfigKey.currentVersion + ".0");
        if (isNewVersion) {
            updateOk.setVisible(false);
            updateNotNewVersionTitle.setVisible(false);
            updateLate.setOnMouseClicked(event -> {
                callBack.onCancel();
                this.currentStage.close();
            });
            updateNow.setOnMouseClicked(event -> {
                callBack.onConfirm();

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
