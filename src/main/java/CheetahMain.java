import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioServerFactory;
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
            NioHPCClientFactory.destroy();
            NioServerFactory.destroy();
            TaskExecutorPoolManager.getInstance().destroyAll();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
