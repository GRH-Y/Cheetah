package test;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceiver;
import connect.network.nio.NioSender;
import ui.common.LogFx;

import javax.net.ssl.SSLEngine;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TestConnect extends NioClientTask {

    public TestConnect(String host, int port) {
        setAddress(host, port, false);
        setReceive(new NioReceiver());
    }

    @Override
    protected void onConnectCompleteChannel(boolean isConnect, SocketChannel channel, SSLEngine sslEngine) throws IOException {
        LogFx.getInstance().printLog("host = " + getHost() + ":" + getPort());
        if (isConnect) {
            setSender(new NioSender(channel));
            getSender().sendData("ping".getBytes());
        }
        LogFx.getInstance().printLog(isConnect ? "链接成功" : "链接失败");
    }
}
