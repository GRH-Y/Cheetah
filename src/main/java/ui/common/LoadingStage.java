package ui.common;

import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadingStage {
    private LoadingStage() {
    }

    public static Stage createLoad(Stage stage) {
        Stage dialogStage = new Stage();
        dialogStage.initOwner(stage);
        dialogStage.initStyle(StageStyle.UNDECORATED);
        dialogStage.initStyle(StageStyle.TRANSPARENT);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        ProgressIndicator loading = new ProgressIndicator();
        loading.setProgress(-1f);
        Scene scene = new Scene(loading);
        dialogStage.setScene(scene);
        return dialogStage;
    }
}
