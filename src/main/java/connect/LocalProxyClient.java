package connect;

import connect.network.nio.NioClientTask;
import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioSender;
import log.LogDog;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public class LocalProxyClient extends NioClientTask implements ICloseListener {

    private ConnectRemoteProxyServer connectRemoteProxyServer;

    public LocalProxyClient(SocketChannel channel, String remoteHost, int remotePort) {
        super(channel, null);
        setSender(new NioSender(channel));
        connectRemoteProxyServer = new ConnectRemoteProxyServer(getSender(), remoteHost, remotePort);
        connectRemoteProxyServer.setOnCloseListener(this);
        NioHPCClientFactory.getFactory().addTask(connectRemoteProxyServer);
        setReceive(new RemoteRequestReceive(connectRemoteProxyServer.getSender()));
    }

    @Override
    protected void onConnectCompleteChannel(boolean isConnect, SocketChannel channel, SSLEngine sslEngine) {
        LogDog.d("==> has client connect ing ... status = " + isConnect);
    }


    @Override
    protected void onCloseClientChannel() {
        LogDog.d("==> has client close ing !!! ");
        NioHPCClientFactory.getFactory().removeTask(connectRemoteProxyServer);
    }

    @Override
    public void onClose(String host) {
        NioHPCClientFactory.getFactory().removeTask(this);
    }
}