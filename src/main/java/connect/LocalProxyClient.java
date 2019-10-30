package connect;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import log.LogDog;

import java.nio.channels.SocketChannel;

public class LocalProxyClient extends NioClientTask {
    private ConnectRemoteProxyServer connectRemoteProxyServer;

    public LocalProxyClient(SocketChannel channel, String host, int port) {
        super(channel);
        setConnectTimeout(0);
        setSender(new RequestSender(this));
        setReceive(new RequestReceive(this, "onReceiveRequestData"));
        connectRemoteProxyServer = new ConnectRemoteProxyServer(getSender(), host, port);
        NioHPCClientFactory.getFactory().addTask(connectRemoteProxyServer);
    }

    @Override
    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
        LogDog.d("==> has client connect ing ... status = " + isConnect);
    }

    private void onReceiveRequestData(byte[] data) {
        LogDog.d("==> Receive Local Request data !!!");
        connectRemoteProxyServer.getSender().sendData(data);
    }
}