package ui.controller;

import com.sun.javafx.application.PlatformImpl;
import config.AnalysisConfig;
import config.ConfigKey;
import connect.http.server.MultipleProxyServer;
import connect.network.base.RequestMode;
import connect.network.nio.NioClientFactory;
import connect.network.nio.NioServerFactory;
import connect.network.xhttp.XHttpConnect;
import connect.network.xhttp.entity.XRequest;
import connect.network.xhttp.entity.XResponse;
import connect.socks5.server.Socks5Server;
import cryption.DataSafeManager;
import cryption.EncryptionType;
import cryption.RSADataEnvoy;
import intercept.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import logic.UIUpdateClient;
import storage.FileHelper;
import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import util.NetUtils;
import util.StringEnvoy;
import util.joggle.JavKeep;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Properties;

public class ControllerMain {

    @FXML
    private ImageView ivConnect;

    @FXML
    private ImageView ivBg;

    @FXML
    private Label mainTestConnect;

    @FXML
    private Label mainCheckUpdate;

    @FXML
    private Label mainVersion;

    private final String CURRENT_COMMAND = "CheetahMain";

    private volatile boolean isOpen = false;
    private final String loHost = "127.0.0.1";


    private String localHost;
    private int proxyPort;
    private int socks5Port;

    private String updateHost;
    private int updatePort;

    private Stage mStage = null;
    //当前工作目录
    private String currentWorkDir = null;
    private String currentCommand = null;

    private boolean isCheckUpdate = false;

    public void resetUpdateStatus() {
        isCheckUpdate = false;
        PlatformImpl.runLater(() -> {
            mainVersion.setLayoutX(149);
            mainVersion.setText("v" + ConfigKey.currentVersion + ".0");
        });
    }

    public static void showLoginScene(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = BaseController.showScene(stage, "res/layout_main.fxml", "wait connect ...");
        ControllerMain controllerMain = fxmlLoader.getController();
        controllerMain.init(stage);
    }

    private String initEnv(String configFile) {
        String filePath = null;
        if (CURRENT_COMMAND.equals(currentCommand)) {
            //ide运行模式，则不创建文件
            URL url = getClass().getClassLoader().getResource(configFile);
            if (url != null) {
                filePath = url.getPath();
            }
        } else {
            filePath = currentWorkDir + configFile;
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
        String interceptFileName = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_INTERCEPT);
        String proxyFileName = AnalysisConfig.getInstance().getValue(ConfigKey.FILE_PROXY);
        String interceptPath = initEnv(interceptFileName);
        //添加 interceptTable.dat 文件的修改监听
        InterceptFileChangeListener interceptFileChangeListener = new InterceptFileChangeListener(interceptPath, interceptFileName);
        WatchFileManager.getInstance().addWatchFile(interceptFileChangeListener);
        //添加 proxyTable.dat 文件的修改监听
        String proxyPath = initEnv(proxyFileName);
        ProxyFileChangeListener proxyFileChangeListener = new ProxyFileChangeListener(proxyPath, proxyFileName);
        WatchFileManager.getInstance().addWatchFile(proxyFileChangeListener);
    }

    private void init(Stage stage) {
        this.mStage = stage;
        mStage.setUserData(this);
        Properties properties = System.getProperties();
        currentCommand = properties.getProperty(ConfigKey.KEY_COMMAND);
        currentWorkDir = properties.getProperty(ConfigKey.KEY_USER_DIR) + File.separator;
        //解析配置文件
        String configPath = initEnv(ConfigKey.FILE_CONFIG);
        AnalysisConfig.getInstance().analysis(configPath);
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
        DataSafeManager.getInstance().init();

        localHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_LOCAL_HOST);
        proxyPort = AnalysisConfig.getInstance().getIntValue(ConfigKey.CONFIG_PROXY_LOCAL_PORT);
        socks5Port = AnalysisConfig.getInstance().getIntValue(ConfigKey.CONFIG_SOCKS5_LOCAL_PORT);
        updateHost = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_REMOTE_UPDATE_HOST);
        updatePort = AnalysisConfig.getInstance().getIntValue(ConfigKey.CONFIG_REMOTE_UPDATE_PORT);
        String imageUrl = AnalysisConfig.getInstance().getValue(ConfigKey.CONFIG_IMAGE);

        if (StringEnvoy.isEmpty(localHost) || "auto".equals(localHost)) {
            localHost = NetUtils.getLocalIp("eth0");
        }

        mainCheckUpdate.setOnMouseClicked(event -> {
            if (isCheckUpdate) {
                return;
            }
            mainVersion.setLayoutX(105);
            mainVersion.setText("Checking updates !!!");
            isCheckUpdate = true;
            //检查版本更新
            initUpdate(true);
        });
        mainTestConnect.setOnMouseClicked(event -> {
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
                    Image noConnectImg = new Image("res/ic_no_connect.png");
                    ivConnect.setImage(noConnectImg);
                    mStage.setTitle("no connect");
                    isOpen = false;
                } else {
                    boolean isServerMode = AnalysisConfig.getInstance().getBooleanValue(ConfigKey.CONFIG_IS_SERVER_MODE);
                    if (!isServerMode) {
                        try {
                            initServer();
                            isOpen = true;
                            Image connectImg = new Image("res/ic_connect.png");
                            ivConnect.setImage(connectImg);
                            mStage.setTitle("listener: " + localHost + ":" + proxyPort);
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

        mainVersion.setText("v" + ConfigKey.currentVersion + ".0");
        //检查版本更新
        initUpdate(false);
    }

    private void initServer() {
        //open proxy server
        NioServerFactory.getFactory().open();
        MultipleProxyServer httpProxyServer = new MultipleProxyServer();
        httpProxyServer.setAddress(localHost, proxyPort);
        NioServerFactory.getFactory().addTask(httpProxyServer);
        if (!loHost.equals(localHost)) {
            MultipleProxyServer loServer = new MultipleProxyServer();
            loServer.setAddress(loHost, proxyPort);
            NioServerFactory.getFactory().addTask(loServer);
        }

        //open connect.socks5 proxy server
        Socks5Server socks5Server = new Socks5Server();
        socks5Server.setAddress(localHost, socks5Port);
        NioServerFactory.getFactory().addTask(socks5Server);
        socks5Server = new Socks5Server();
        socks5Server.setAddress(loHost, socks5Port);
        NioServerFactory.getFactory().addTask(socks5Server);
    }

    private void initUpdate(boolean isNeedShowDialog) {
        NioClientFactory.getFactory().open();
        UIUpdateClient updateHandleClient = new UIUpdateClient(mStage, updateHost, updatePort, isNeedShowDialog);
        updateHandleClient.checkUpdate();
        NioClientFactory.getFactory().open();
        NioClientFactory.getFactory().addTask(updateHandleClient);
    }


    private class ShowImageTask extends BaseLoopTask {

        private String imageUrl;
        private File imageFile = null;

        public ShowImageTask(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        @Override
        protected void onRunLoopTask() {
            String bgImagePath = initEnv(ConfigKey.FILE_BG_IMAGE);
            imageFile = new File(bgImagePath);
            if (imageFile.exists()) {
                Image image = new Image("file:" + imageFile.getAbsolutePath());
                ivBg.setImage(image);
            } else {
                LinkedHashMap<Object, Object> property = new LinkedHashMap<>();
                property.put("User-Agent", "Mozilla/9.0 (Windows NT 11.0; Win64; x64; rv:71.0) Gecko/20100101 Firefox/71.0");
                XRequest request = new XRequest();
                request.setUrl(imageUrl);
                request.setCallBackMethod("onReceiveData");
                request.setCallBackTarget(this);
                request.setRequestMode(RequestMode.GET);
                request.setResultType(byte[].class);
                request.setRequestProperty(property);
                XHttpConnect.getInstance().submitRequest(request);
            }
            TaskExecutorPoolManager.getInstance().closeTask(this);
        }

        @JavKeep
        private void onReceiveData(XRequest request, XResponse response, Throwable e) {
            if (e != null) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    e.printStackTrace();
                }
                XHttpConnect.getInstance().submitRequest(request);
            } else {
                if (response.getHttpData() != null) {
                    byte[] img = response.getHttpData();
                    FileHelper.writeFile(imageFile, img);
                    Image image = new Image("file:" + imageFile.getAbsolutePath());
                    ivBg.setImage(image);
                }
            }
        }

    }
}
