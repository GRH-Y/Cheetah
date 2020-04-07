package test;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioReceiver;
import connect.network.nio.NioSender;
import ui.common.LogFx;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class TestConnect extends NioClientTask {

    public TestConnect(String host, int port) {
        setAddress(host, port, false);
        setReceive(new NioReceiver());
    }

    @Override
    protected void onConnectCompleteChannel(SocketChannel channel) throws IOException {
        LogFx.getInstance().printLog("host = " + getHost() + ":" + getPort());
        setSender(new NioSender(channel));
        getSender().sendData("ping".getBytes());
        LogFx.getInstance().printLog("链接成功");
    }

    @Override
    protected void onRecovery() {
        NioHPCClientFactory.destroy();
    }
}
