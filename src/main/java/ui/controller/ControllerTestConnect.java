package ui.controller;

import connect.network.nio.NioHPCClientFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import test.PingTask;
import test.TestConnect;
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


    public static void showTestConnectScene(String ip, String port) throws IOException {
        if (isShow) {
            return;
        }
        Stage newStage = new Stage();
        FXMLLoader fxmlLoader = BaseController.showScene(newStage, "layout_test_connect.fxml", "Test Connect");
        ControllerTestConnect controllerTestConnect = fxmlLoader.getController();
        controllerTestConnect.initView(ip, port);
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
                initConnect();
                ping();
                btnTCStart.setText("Stop");
                isOpen = true;
            }
        });
    }

    private void initConnect() {
        TestConnect connect = new TestConnect(tfTCAddress.getText(), Integer.parseInt(tfTCPort.getText()));
        NioHPCClientFactory.getFactory(1).open();
        NioHPCClientFactory.getFactory(1).addTask(connect);
    }

    private void ping() {
        pingTask = new PingTask(tfTCAddress.getText());
        pingTask.startPing();
    }


}
