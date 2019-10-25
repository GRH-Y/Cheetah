package ui.common;

import javafx.scene.control.Alert;

public class DialogStage {

    private DialogStage(){}

    //    弹出一个信息对话框
    public static void showDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
//        alert.initOwner(null);
        alert.setContentText(message);
        alert.show();
    }
}
