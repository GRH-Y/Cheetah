package ui.common;

import com.sun.javafx.application.PlatformImpl;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class LogFx {
    private TextFlow logText;

    private static LogFx logFx = null;

    public static LogFx getInstance() {
        synchronized (LogFx.class) {
            if (logFx == null) {
                synchronized (LogFx.class) {
                    if (logFx == null) {
                        logFx = new LogFx();
                    }
                }
            }
        }
        return logFx;
    }

    private LogFx() {
    }

    public void init(TextFlow logText) {
        this.logText = logText;
    }

    public void printLog(String log) {
        Text text = new Text(log + "\n");
        text.setFill(Color.LIMEGREEN);
        PlatformImpl.runLater(() -> logText.getChildren().add(text));
    }
}
