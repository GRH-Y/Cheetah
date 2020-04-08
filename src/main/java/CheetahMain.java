import connect.network.nio.NioClientFactory;
import connect.network.nio.NioServerFactory;
import intercept.WatchConfigFileTask;
import javafx.application.Application;
import javafx.stage.Stage;
import task.executor.TaskExecutorPoolManager;
import ui.controller.ControllerConnect;

public class CheetahMain extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        ControllerConnect.showLoginScene(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            NioClientFactory.destroy();
            NioServerFactory.destroy();
            WatchConfigFileTask.getInstance().destroy();
            TaskExecutorPoolManager.getInstance().destroyAll();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

}

