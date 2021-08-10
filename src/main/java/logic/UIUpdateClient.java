package logic;

import com.sun.javafx.application.PlatformImpl;
import connect.http.client.UpdateHandleClient;
import connect.joggle.IUpdateConfirmCallBack;
import connect.network.nio.NioClientFactory;
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
    public void onClientCheckVersion(boolean isHasNewVersion, IUpdateConfirmCallBack callBack) {
        if (!isHasNewVersion) {
            NioClientFactory.getFactory().removeTask(this);
            if (!isNeedShowDialog) {
                return;
            }
        }
        PlatformImpl.runLater(() -> {
            //提示更新框
            try {
                ControllerUpdate.showUpdate(isHasNewVersion, callBack);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onUpdateComplete() {
        super.onUpdateComplete();
        System.exit(0);
    }

    @Override
    public void onConfirm() {
        super.onConfirm();
    }

    @Override
    public void onCancel() {
        super.onCancel();
        ControllerUpdate.close();
    }

    @Override
    protected void onRecovery() {
        super.onRecovery();
        ControllerMain main = (ControllerMain) mainStage.getUserData();
        main.resetUpdateStatus();
    }
}
