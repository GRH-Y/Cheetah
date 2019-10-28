package ui.controller;

import config.AnalysisConfig;
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
import storage.FileHelper;
import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import util.IoEnvoy;
import util.NetUtils;
import util.StringEnvoy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

public class ControllerConnect {

    @FXML
    private Button connectButton;

    @FXML
    private ImageView ivConnectBg;

    @FXML
    private TextField tfRemoteHost;

    @FXML
    private TextField tfRemotePort;

    @FXML
    private TextField tfLocalHost;

    @FXML
    private TextField tfLocalPort;

    @FXML
    private MenuItem miTestConnect;

    @FXML
    private MenuItem miCheckUpdate;

    @FXML
    private MenuItem miAbout;

    private static boolean isOpen = false;
    private static final String FILE_CONFIG = "config.cfg";
    private final String defaultPort = "8877";

    public static void showLoginScene(Stage stage) throws IOException {
        ControllerConnect controllerConfig = BaseController.showScene(stage, "layout_connect.fxml", "Cheetah");
        controllerConfig.init(stage);
    }

    private String initEnv(String configFile) {
        Properties properties = System.getProperties();
        String value = properties.getProperty("sun.java.command");

        String filePath = null;

        if ("CheetahMain".equals(value)) {
            //ide运行模式，则不创建文件
            URL url = getClass().getClassLoader().getResource(configFile);
            filePath = url.getPath();
        } else {
            String dirPath = properties.getProperty("user.dir");
            File atFile = new File(dirPath, configFile);
            if (atFile.exists()) {
                if (atFile.length() > 1024 * 1024) {
                    LogDog.e("Profile is too large > 1M !!!");
                } else {
                    filePath = atFile.getAbsolutePath();
                }
            } else {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFile);
                try {
                    byte[] data = IoEnvoy.tryRead(inputStream);
                    FileHelper.writeFileMemMap(atFile, data, false);
                    filePath = atFile.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    private void init(Stage stage) {
        String configFile = initEnv(FILE_CONFIG);
        String host = null;
        String port = null;
        String image = null;
        Map<String, String> configMap = AnalysisConfig.analysis(configFile);
        if (configMap != null) {
            host = configMap.get("host");
            port = configMap.get("port");
            image = configMap.get("image");
        }
        if (StringEnvoy.isEmpty(host) || "auto".equals(host)) {
            host = NetUtils.getLocalIp("eth2");
        }
        if (StringEnvoy.isEmpty(port)) {
            port = defaultPort;
        }

        tfLocalHost.setText(host);
        tfLocalPort.setText(port);

        miAbout.setOnAction(event -> LogDog.d("about"));
        miCheckUpdate.setOnAction(event -> LogDog.d("CheckUpdate"));
        miTestConnect.setOnAction(event -> {
            try {
                String remoteHost = tfRemoteHost.getText();
                String remotePort = tfRemotePort.getText();
                ControllerTestConnect.showTestConnectScene(stage, remoteHost, remotePort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        if (isOpen) {
            connectButton.setText("Stop connect");
        }
        connectButton.setOnAction(event -> {
            if (isOpen) {
                NioServerFactory.getFactory().close();
                connectButton.setText("Start connect");
                isOpen = false;
            } else {
                String localHost = tfLocalHost.getText();
                String localPort = tfLocalPort.getText();
                String remoteHost = tfRemoteHost.getText();
                String remotePort = tfRemotePort.getText();
                initServer(localHost, Integer.parseInt(localPort), remoteHost, Integer.parseInt(remotePort));
                isOpen = true;
                connectButton.setText("Stop connect");
            }
        });
        //显示首页图片
        if (StringEnvoy.isNotEmpty(image)) {
            ShowImageTask showImageTask = new ShowImageTask(image);
            TaskExecutorPoolManager.getInstance().runTask(showImageTask, null);
        }
    }

    private void initServer(String localHost, int localPort, String remoteHost, int remotePort) {
        LocalProxyServer localProxyServer = new LocalProxyServer(localHost, localPort, remoteHost, remotePort);
        NioServerFactory.getFactory().open();
        NioServerFactory.getFactory().addTask(localProxyServer);
    }


    private class ShowImageTask extends BaseLoopTask {

        private String path;

        public ShowImageTask(String path) {
            this.path = path;
        }

        @Override
        protected void onRunLoopTask() {
            ivConnectBg.setImage(new Image(path));
            TaskExecutorPoolManager.getInstance().closeTask(this);
        }
    }

}
