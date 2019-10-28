package connect;

import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioServerTask;
import connect.network.nio.SimpleSendTask;
import log.LogDog;
import util.StringEnvoy;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class LocalProxyServer extends NioServerTask {

    private String remoteHost;
    private int remotePort;

    public LocalProxyServer(String localHost, int localPort, String remoteHost, int remotePort) {
        if (StringEnvoy.isEmpty(localHost) || localPort < 0 || StringEnvoy.isEmpty(remoteHost) || remotePort < 0) {
            throw new IllegalArgumentException("localHost，localPort，remoteHost or remotePort is Illegal");
        }
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        setAddress(localHost, localPort);
    }

    @Override
    protected void onConfigServer(boolean isSuccess, ServerSocketChannel channel) {
        LogDog.d("==> Local Proxy Server status = " + isSuccess);
        LogDog.d("==> HttpProxy Server address = " + getServerHost() + ":" + getServerPort());
        if (isSuccess) {
            NioHPCClientFactory.getFactory(1).open();
        }
    }

    @Override
    protected void onAcceptServerChannel(SocketChannel channel) {
        LocalProxyClient proxyServer = new LocalProxyClient(channel, remoteHost, remotePort);
        NioHPCClientFactory.getFactory().addTask(proxyServer);
    }

    @Override
    protected void onCloseServerChannel() {
        LogDog.e("==> Local Proxy Server close ing... !!! ");
        NioHPCClientFactory.destroy();
        SimpleSendTask.getInstance().close();
    }
}
