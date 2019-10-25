package ui.controller;

import connect.LocalProxyServer;
import connect.network.nio.NioServerFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import log.LogDog;
import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import util.NetUtils;

import java.io.IOException;

public class ControllerConnect {

    @FXML
    private Button connectButton;

    @FXML
    private ImageView ivConnectBg;

    @FXML
    private TextField txfAddress;

    @FXML
    private TextField txfPort;

    @FXML
    private MenuItem miTestConnect;

    @FXML
    private MenuItem miCheckUpdate;

    @FXML
    private MenuItem miAbout;

    private static boolean isOpen = false;

    public static void showLoginScene(Stage stage) throws IOException {
        ControllerConnect controllerConfig = BaseController.showScene(stage, "layout_connect.fxml", "Cheetah");
        controllerConfig.init(stage);
    }

    private void init(Stage stage) {
        miAbout.setOnAction(event -> LogDog.d("about"));
        miCheckUpdate.setOnAction(event -> LogDog.d("CheckUpdate"));
        miTestConnect.setOnAction(event -> {
            try {
                String remoteHost = txfAddress.getText();
                String remotePort = txfPort.getText();
                ControllerTestConnect.showTestConnectScene(stage, remoteHost, remotePort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (isOpen) {
            connectButton.setText("stop connect");
        }
        connectButton.setOnAction(event -> {
            if (isOpen) {
                NioServerFactory.getFactory().close();
                connectButton.setText("start connect");
                isOpen = false;
            } else {
                initLocalServer();
                isOpen = true;
                connectButton.setText("stop connect");
            }
        });
        ShowImageTask showImageTask = new ShowImageTask("http://b-ssl.duitang.com/uploads/item/201401/17/20140117230542_eevad.jpeg");
        TaskExecutorPoolManager.getInstance().runTask(showImageTask, null);
    }

    private void initLocalServer() {
        LocalProxyServer localProxyServer = new LocalProxyServer();
        String remoteHost = txfAddress.getText();
        String remotePort = txfPort.getText();
        localProxyServer.setRemoteProxyServer(remoteHost, Integer.parseInt(remotePort));
        String host = NetUtils.getLocalIp("eth2");
        int defaultPort = 8877;
        localProxyServer.setAddress(host, defaultPort);
        NioServerFactory.getFactory().open();
        NioServerFactory.getFactory().addTask(localProxyServer);
        LogDog.d("==> HttpProxy Server address = " + host + ":" + defaultPort);
    }


    private class ShowImageTask extends BaseLoopTask {

        private String path;

        public ShowImageTask(String path) {
            this.path = path;
        }

        @Override
        protected void onRunLoopTask() {
//            Platform.runLater(() -> dialogStage.show());
            ivConnectBg.setImage(new Image(path));
            TaskExecutorPoolManager.getInstance().closeTask(this);
//            Platform.runLater(() -> dialogStage.close());
        }
    }

}
