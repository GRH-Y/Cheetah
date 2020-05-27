package ui.controller;

import config.AnalysisConfig;
import config.ConfigKey;
import connect.clinet.LocalProxyServer;
import connect.network.nio.NioServerFactory;
import cryption.EncryptionType;
import cryption.RSADataEnvoy;
import intercept.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Properties;

public class ControllerConnect {

//    @FXML
//    private Button connectButton;

    @FXML
    private ImageView ivConnect;

    @FXML
    private ImageView ivBg;

    @FXML
    private MenuItem miTestConnect;

    @FXML
    private MenuItem miCheckUpdate;

    @FXML
    private MenuItem miAbout;

    private volatile boolean isOpen = false;
    private final String defaultPort = "8877";
    private final String loHost = "127.0.0.1";

    private String localHost;
    private String localPort;

    private Stage mStage = null;

    public static void showLoginScene(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = BaseController.showScene(stage, "layout_connect.fxml", "wait connect ...");
        ControllerConnect controllerConnect = fxmlLoader.getController();
        controllerConnect.init(stage);
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

    private void init(Stage stage) {
        this.mStage = stage;
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

        localHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_LOCAL_HOST);
        localPort = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_LOCAL_PORT);
        String remoteHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_HOST);
        String remotePort = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_PORT);
        String image = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_IMAGE);

        if (StringEnvoy.isEmpty(localHost) || "auto".equals(localHost)) {
            localHost = NetUtils.getLocalIp("eth0");
        }
        if (StringEnvoy.isEmpty(localPort)) {
            localPort = defaultPort;
        }

        miAbout.setOnAction(event -> LogDog.d("About : belong to the world!"));
        miCheckUpdate.setOnAction(event -> LogDog.d("Update function development..."));
        miTestConnect.setOnAction(event -> {
            try {
                ControllerTestConnect.showTestConnectScene(remoteHost, remotePort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        //设置鼠标点击事件
        ivConnect.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                //右点击
            } else {
                //左点击
                if (isOpen) {
                    NioServerFactory.getFactory().close();
                    Image noConnectImg = new Image("ic_no_connect.png");
                    ivConnect.setImage(noConnectImg);
                    mStage.setTitle("no connect");
                    isOpen = false;
                } else {
                    boolean isServerMode = AnalysisConfig.getInstance().getBooleanValue(ConfigKey.CONFIG_IS_SERVER_MODE);
                    if (!isServerMode) {
                        try {
                            initServer(localHost, Integer.parseInt(localPort), remoteHost, Integer.parseInt(remotePort));
                            isOpen = true;
                            Image connectImg = new Image("ic_connect.png");
                            ivConnect.setImage(connectImg);
                            mStage.setTitle("listener: " + localHost + ":" + localPort);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
        NioServerFactory.getFactory().open();
        NioServerFactory.getFactory().addTask(new LocalProxyServer(localHost, localPort, remoteHost, remotePort));
        if (!loHost.equals(localHost)) {
            NioServerFactory.getFactory().addTask(new LocalProxyServer(loHost, localPort, remoteHost, remotePort));
        }
    }

    private class ShowImageTask extends BaseLoopTask {

        private String path;

        public ShowImageTask(String path) {
            this.path = path;
        }

        @Override
        protected void onRunLoopTask() {
            boolean isConnect = isNodeReachable("163.177.151.110", 80);
            if (isConnect) {
                ivBg.setImage(new Image(path));
                TaskExecutorPoolManager.getInstance().closeTask(this);
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean isNodeReachable(String hostname, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.setSoTimeout(500);
            socket.connect(new InetSocketAddress(hostname, port), 500);
            return socket.isConnected();
        } catch (Throwable e) {
            LogDog.d("network error !!!");
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

}
