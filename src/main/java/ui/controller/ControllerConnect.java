package ui.controller;

import config.AnalysisConfig;
import config.ConfigKey;
import connect.clinet.LocalProxyServer;
import connect.network.nio.NioServerFactory;
import cryption.EncryptionType;
import cryption.RSADataEnvoy;
import intercept.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
    private final String defaultPort = "8877";

    public static void showLoginScene(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = BaseController.showScene(stage, "layout_connect.fxml", "Cheetah");
        ControllerConnect controllerConnect = fxmlLoader.getController();
        controllerConnect.init();
    }

    private String initEnv(String configFile) {
        Properties properties = System.getProperties();
        String value = properties.getProperty("sun.java.command");

        String filePath = null;

        if ("CheetahMain".equals(value)) {
            //ide运行模式，则不创建文件
            URL url = getClass().getClassLoader().getResource(configFile);
            if (url != null) {
                filePath = url.getPath();
            }
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

    private void initRSA() {
        String publicKey = initEnv(ConfigKey.FILE_PUBLIC_KEY);
        String privateKey = initEnv(ConfigKey.FILE_PRIVATE_KEY);
        RSADataEnvoy.getInstance().init(publicKey, privateKey);
    }

    private void initInterceptFilter() {
        boolean intercept = AnalysisConfig.getInstance().getBooleanValue(ConfigKey.CONFIG_INTERCEPT);
        if (intercept) {
            String configInterceptFile = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_INTERCEPT);
            String interceptFile = initEnv(configInterceptFile);
            //初始化地址过滤器
            BuiltInInterceptFilter proxyFilter = new BuiltInInterceptFilter();
            proxyFilter.init(interceptFile);
            InterceptFilterManager.getInstance().addFilter(proxyFilter);
        }
    }

    private void initProxyFilter() {
        String configProxyFile = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_PROXY);
        String proxyFile = initEnv(configProxyFile);
        //初始化地址过滤器
        ProxyFilterManager.getInstance().loadProxyTable(proxyFile);
    }

    private void initWatchFile() {
        Properties properties = System.getProperties();
        String value = properties.getProperty("sun.java.command");
        String dirPath = properties.getProperty("user.dir");
        String interceptFileName = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_INTERCEPT);
        String proxyFileName = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_PROXY);
        if ("CheetahMain".equals(value)) {
            //idea模式下
            dirPath = dirPath + "\\out\\production\\resources";
        }
        //添加 interceptTable.dat 文件的修改监听
        InterceptFileChangeListener interceptFileChangeListener = new InterceptFileChangeListener(dirPath, interceptFileName);
        WatchConfigFileTask.getInstance().addWatchFile(interceptFileChangeListener);
        //添加 proxyTable.dat 文件的修改监听
        ProxyFileChangeListener proxyFileChangeListener = new ProxyFileChangeListener(dirPath, proxyFileName);
        WatchConfigFileTask.getInstance().addWatchFile(proxyFileChangeListener);
    }

    private void init() {
        String configFile = initEnv(ConfigKey.FILE_CONFIG);
        //解析配置文件
        AnalysisConfig.getInstance().analysis(configFile);
        //初始化拦截黑名单过滤器
        initInterceptFilter();
        //初始化强制代理管理器
        initProxyFilter();
        //初始化监听黑名单文件修改
        initWatchFile();
        //如果是RSA加密则初始化公钥和私钥
        String encryption = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_ENCRYPTION_MODE);
        if (EncryptionType.RSA.name().equals(encryption)) {
            initRSA();
        }

        String localHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_LOCAL_HOST);
        String localPort = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_LOCAL_PORT);
        String remoteHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_HOST);
        String remotePort = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_PORT);
        String image = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_IMAGE);

        if (StringEnvoy.isEmpty(localHost) || "auto".equals(localHost)) {
            localHost = NetUtils.getLocalIp("eth0");
        }
        if (StringEnvoy.isEmpty(localPort)) {
            localPort = defaultPort;
        }

        tfLocalHost.setText(localHost);
        tfLocalPort.setText(localPort);
        tfRemoteHost.setText(remoteHost);
        tfRemotePort.setText(remotePort);

        miAbout.setOnAction(event -> LogDog.d("about"));
        miCheckUpdate.setOnAction(event -> LogDog.d("CheckUpdate"));
        miTestConnect.setOnAction(event -> {
            try {
                ControllerTestConnect.showTestConnectScene(tfRemoteHost.getText(), tfRemotePort.getText());
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
                boolean isServerMode = AnalysisConfig.getInstance().getBooleanValue(ConfigKey.CONFIG_IS_SERVER_MODE);
                if (!isServerMode) {
                    try {
                        initServer(tfLocalHost.getText(), Integer.parseInt(tfLocalPort.getText()), tfRemoteHost.getText(), Integer.parseInt(tfRemotePort.getText()));
                        isOpen = true;
                        connectButton.setText("Stop connect");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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
