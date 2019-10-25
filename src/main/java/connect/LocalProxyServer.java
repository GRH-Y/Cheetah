package connect;

import connect.network.nio.NioHPCClientFactory;
import connect.network.nio.NioServerTask;
import connect.network.nio.SimpleSendTask;
import log.LogDog;

import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class LocalProxyServer extends NioServerTask {

    private String remoteAddress;
    private int remotePort;

    @Override
    protected void onConfigServer(boolean isSuccess, ServerSocketChannel channel) {
        LogDog.d("==> Local Proxy Server status = " + isSuccess);
        if (isSuccess) {
            NioHPCClientFactory.getFactory(1).open();
        }
    }

    public void setRemoteProxyServer(String remoteAddress, int remotePort) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
    }

    @Override
    protected void onAcceptServerChannel(SocketChannel channel) {
        LocalProxyClient proxyServer = new LocalProxyClient(channel, remoteAddress, remotePort);
        NioHPCClientFactory.getFactory().addTask(proxyServer);
    }

    @Override
    protected void onCloseServerChannel() {
        LogDog.e("==> Local Proxy Server close ing... !!! ");
        NioHPCClientFactory.destroy();
        SimpleSendTask.getInstance().close();
    }
}
