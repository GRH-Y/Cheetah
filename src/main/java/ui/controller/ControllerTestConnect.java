package ui.controller;

import connect.network.nio.NioHPCClientFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import javafx.scene.control.Button;
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
                NioHPCClientFactory.destroy();
                btnTCStart.setText("Start");
                isOpen = false;
            } else {
                initConnect();
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


}
