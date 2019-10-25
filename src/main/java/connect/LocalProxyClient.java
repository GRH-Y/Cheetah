package connect;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioReceive;
import log.LogDog;

import java.nio.channels.SocketChannel;

public class LocalProxyClient extends NioClientTask {
    private ConnectRemoteProxyServer connectRemoteProxyServer;

    public LocalProxyClient(SocketChannel channel, String host, int port) {
        super(channel);
        setConnectTimeout(8000);
        setSender(new RequestSender());
        setReceive(new NioReceive(this, "onReceiveRequestData"));
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