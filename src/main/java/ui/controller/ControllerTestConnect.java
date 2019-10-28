package ui.controller;

import connect.network.nio.NioHPCClientFactory;
import javafx.fxml.FXML;
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
    private Button btnTCBack;

    @FXML
    private Button btnTCStart;

    @FXML
    private TextFlow tfLogContent;

    @FXML
    private TextField tfTCAddress;
    @FXML
    private TextField tfTCPort;

    private boolean isOpen = false;

    private PingTask pingTask = null;

    public static void showTestConnectScene(Stage stage, String ip, String port) throws IOException {
        ControllerTestConnect controllerTestConnect = BaseController.showScene(stage, "layout_test_connect.fxml", "Test Connect");
        controllerTestConnect.initView(stage, ip, port);
    }

    private void initView(Stage stage, String ip, String port) {
        LogFx.getInstance().init(tfLogContent);
        tfTCAddress.setText(ip);
        tfTCPort.setText(port);
        //返回上一个界面
        btnTCBack.setOnAction(event -> {
            try {
                ControllerConnect.showLoginScene(stage);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
