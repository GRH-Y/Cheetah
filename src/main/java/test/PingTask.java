package test;

import log.LogDog;
import task.executor.BaseLoopTask;
import task.executor.TaskExecutorPoolManager;
import task.executor.joggle.ITaskContainer;
import ui.common.LogFx;
import util.IoEnvoy;
import util.StringEnvoy;

import java.util.Properties;

public class PingTask extends BaseLoopTask {

    private String host;
    private ITaskContainer container;
    private String format = "UTF-8";

    public PingTask(String host) {
        if (StringEnvoy.isEmpty(host)) {
            throw new RuntimeException(" host is null !!!");
        }
        this.host = host;
        container = TaskExecutorPoolManager.getInstance().createLoopTask(this, null);
    }

    @Override
    protected void onInitTask() {
        Properties properties = System.getProperties();
        String osName = properties.getProperty("os.name");
        if (osName.contains("Windows")) {
            format = "GBK";
        }
    }

    @Override
    protected void onRunLoopTask() {
        try {
            Runtime run = Runtime.getRuntime();
            Process process = run.exec("ping " + host + " -n 1");
            byte[] data = IoEnvoy.tryRead(process.getInputStream());
            if (data != null) {
                String ping = new String(data, format);
                int ttlIIndex = ping.indexOf("TTL");
                if (ttlIIndex > 0) {
                    int timeIndex = ping.lastIndexOf(" ", ttlIIndex - 2);
                    String time = ping.substring(timeIndex, ttlIIndex);
                    LogDog.d(" ==> ping " + time);
                    LogFx.getInstance().printLog("ping " + time);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        container.getTaskExecutor().sleepTask(2000);
    }

    public void startPing() {
        container.getTaskExecutor().startTask();
    }

    public void stopPing() {
        container.getTaskExecutor().stopTask();
    }
}
