package ui.controller;

import config.AnalysisConfig;
import config.ConfigKey;
import connect.network.base.RequestMode;
import connect.network.nio.NioServerFactory;
import connect.network.xhttp.XHttpConnect;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.server.MultipleProxyServer;
import cryption.EncryptionType;
import cryption.RSADataEnvoy;
import intercept.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import log.LogDog;
import storage.FileHelper;
import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import util.IoEnvoy;
import util.NetUtils;
import util.StringEnvoy;
import util.joggle.JavKeep;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
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
        String imageUrl = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_IMAGE);

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
                ControllerTestConnect.showTestConnectScene();
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
                            initServer(localHost, Integer.parseInt(localPort));
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
        if (StringEnvoy.isNotEmpty(imageUrl)) {
            ShowImageTask showImageTask = new ShowImageTask(imageUrl);
            TaskExecutorPoolManager.getInstance().runTask(showImageTask, null);
        }
    }

    private void initServer(String localHost, int localPort) {
        NioServerFactory.getFactory().open();
        MultipleProxyServer httpProxyServer = new MultipleProxyServer();
        httpProxyServer.setAddress(localHost, localPort, false);
        NioServerFactory.getFactory().addTask(httpProxyServer);
        if (!loHost.equals(localHost)) {
            MultipleProxyServer loServer = new MultipleProxyServer();
            loServer.setAddress(loHost, localPort, false);
            NioServerFactory.getFactory().addTask(loServer);
        }
    }


    private class ShowImageTask extends BaseLoopTask {

        private String imageUrl;

        public ShowImageTask(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        protected void onRunLoopTask() {
            Properties properties = System.getProperties();
            String dirPath = properties.getProperty("user.dir");
            File file = new File(dirPath, "bg.jpg");
            if (file.exists()) {
                Image image = new Image("file:" + file.getAbsolutePath());
                ivBg.setImage(image);
            } else {
                Map<String, Object> property = new HashMap<>();
                property.put("User-Agent", "Mozilla/9.0 (Windows NT 11.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
                XRequest request = new XRequest();
                request.setUrl(imageUrl);
                request.setSuccessMethod("onReceiveData");
                request.setErrorMethod("onReceiveError");
                request.setCallBackTarget(this);
                request.setRequestMode(RequestMode.GET);
                request.setResultType(byte[].class);
                request.setRequestProperty(property);
                XHttpConnect.getInstance().submitRequest(request);
            }
            TaskExecutorPoolManager.getInstance().closeTask(this);
        }

        @JavKeep
        private void onReceiveData(XRequest request, XResponse response) {
            if (response.getHttpData() != null) {
                byte[] img = response.getHttpData();
                Properties properties = System.getProperties();
                String dirPath = properties.getProperty("user.dir");
                String filePath = dirPath + File.separator + "bg.jpg";
                FileHelper.writeFile(filePath, img);
                Image image = new Image("file:" + filePath);
                ivBg.setImage(image);
            }
        }

        @JavKeep
        private void onReceiveError(XRequest request, XResponse response) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            XHttpConnect.getInstance().submitRequest(request);
        }
    }
}
