package ui.controller;

import config.AnalysisConfig;
import config.ConfigKey;
import connect.network.nio.NioHPCClientFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import logic.PingTask;
import ui.common.LogFx;

import java.io.IOException;

public class ControllerTestConnect {

    @FXML
    private Button btnClear;

    @FXML
    private Button btnTCStart;

    @FXML
    private TextFlow tfLogContent;

    @FXML
    private TextField tfTCAddress;
    @FXML
    private TextField tfTCPort;

    private boolean isOpen = false;

    private static PingTask pingTask = null;

    public static boolean isShow = false;


    public static void showTestConnectScene() throws IOException {
        if (isShow) {
            return;
        }
        Stage newStage = new Stage();
        newStage.getIcons().add(new Image("ic_logo.png"));
        FXMLLoader fxmlLoader = BaseController.showScene(newStage, "layout_test_connect.fxml", "Test Connect");
        ControllerTestConnect controllerTestConnect = fxmlLoader.getController();
        String remoteHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_PROXY_HOST);
        String remotePort = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_PROXY_PORT);
        controllerTestConnect.initView(remoteHost, remotePort);
        newStage.show();
        newStage.setOnCloseRequest(event -> {
            isShow = false;
            if (pingTask != null) {
                pingTask.stopPing();
            }
        });
        isShow = true;
    }

    private void initView(String ip, String port) {
        LogFx.getInstance().init(tfLogContent);
        tfTCAddress.setText(ip);
        tfTCPort.setText(port);
        //清楚界面输出
        btnClear.setOnAction(event -> {
            tfLogContent.getChildren().clear();
        });
        //开始链接测试
        btnTCStart.setOnAction(event -> {
            if (isOpen) {
                pingTask.stopPing();
                NioHPCClientFactory.destroy();
                btnTCStart.setText("Start");
                isOpen = false;
            } else {
                ping();
                btnTCStart.setText("Stop");
                isOpen = true;
            }
        });
    }


    private void ping() {
        pingTask = new PingTask(tfTCAddress.getText());
        pingTask.startPing();
    }


}
