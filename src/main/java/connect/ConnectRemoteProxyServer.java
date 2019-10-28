package connect;

import connect.network.base.joggle.INetSender;
import connect.network.nio.NioClientTask;
import log.LogDog;

import java.nio.channels.SocketChannel;

public class ConnectRemoteProxyServer extends NioClientTask {
    private INetSender sender;

    public ConnectRemoteProxyServer(INetSender sender, String host, int port) {
        super(host, port);
        this.sender = sender;
        setConnectTimeout(0);
        setSender(new RequestSender());
        setReceive(new RequestReceive(this, "onReceiveRequestData"));
    }

    @Override
    protected void onConfigSocket(boolean isConnect, SocketChannel channel) {
        LogDog.d("==> connect Remote Proxy Server ing ... status = " + isConnect);
    }

    private void onReceiveRequestData(byte[] data) {
        LogDog.d("==> Receive Remote Proxy Server data !!! ");
        sender.sendData(data);
    }
}
