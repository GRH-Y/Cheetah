package test;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioReceive;
import connect.network.nio.NioSender;
import ui.common.LogFx;

import java.nio.channels.SocketChannel;

public class TestConnect extends NioClientTask {

    public TestConnect(String host, int port) {
        super(host, port);
        setConnectTimeout(0);
        setReceive(new NioReceive());
        setSender(new NioSender());
    }

    @Override
    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
        LogFx.getInstance().printLog("host = " + getHost() + ":" + getPort());
        if (isConnect) {
            LogFx.getInstance().printLog("链接成功");
            getSender().sendData("fuck you".getBytes());
        } else {
            LogFx.getInstance().printLog("链接失败");
        }
    }
}
