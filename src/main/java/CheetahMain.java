import config.ConfigKey;
import connect.network.nio.NioClientFactory;
import connect.network.nio.NioServerFactory;
import connect.network.xhttp.XHttpConnect;
import intercept.WatchFileManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import log.LogDog;
import task.executor.TaskExecutorPoolManager;
import ui.controller.ControllerMain;

import java.io.File;

public class CheetahMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        String dir = System.getProperties().getProperty(ConfigKey.KEY_USER_DIR);
        primaryStage.getIcons().add(new Image("file:" + dir + File.separator + "res/ic_logo.png"));
        LogDog.initLogSavePath(dir + File.separator, "http_proxy");
        ControllerMain.showLoginScene(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            NioClientFactory.destroy();
            NioServerFactory.destroy();
            WatchFileManager.getInstance().destroy();
            XHttpConnect.destroy();
            TaskExecutorPoolManager.getInstance().destroyAll();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}

