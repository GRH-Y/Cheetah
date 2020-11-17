package logic;

import com.sun.javafx.application.PlatformImpl;
import connect.client.UpdateHandleClient;
import javafx.stage.Stage;
import ui.controller.ControllerMain;
import ui.controller.ControllerUpdate;

import java.io.IOException;

public class UIUpdateClient extends UpdateHandleClient {

    private Stage mainStage;
    private boolean isNeedShowDialog;

    public UIUpdateClient(Stage mainStage, String host, int port, boolean isNeedShowDialog) {
        super(host, port);
        this.mainStage = mainStage;
        this.isNeedShowDialog = isNeedShowDialog;
    }

    @Override
    public void onUpdateComplete() {
        super.onUpdateComplete();
        PlatformImpl.runLater(() -> {
            //提示更新框
            try {
                ControllerUpdate.showUpdate(mainStage, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onClientCheckVersion(boolean isHasNewVersion) {
        if (!isHasNewVersion && !isNeedShowDialog) {
            return;
        }
        PlatformImpl.runLater(() -> {
            //提示更新框
            try {
                ControllerUpdate.showUpdate(mainStage, false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        ControllerMain main = (ControllerMain) mainStage.getUserData();
        main.resetUpdateStatus();
    }
}
