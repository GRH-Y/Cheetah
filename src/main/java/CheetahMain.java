import connect.network.nio.NioClientFactory;
import connect.network.nio.NioServerFactory;
import connect.network.xhttp.XHttpConnect;
import intercept.WatchConfigFileTask;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import task.executor.TaskExecutorPoolManager;
import ui.controller.ControllerConnect;

public class CheetahMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.getIcons().add(new Image("ic_logo.png"));
        ControllerConnect.showLoginScene(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            NioClientFactory.destroy();
            NioServerFactory.destroy();
            WatchConfigFileTask.getInstance().destroy();
            XHttpConnect.destroy();
            TaskExecutorPoolManager.getInstance().destroyAll();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}

