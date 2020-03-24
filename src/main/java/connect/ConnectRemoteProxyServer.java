package connect;

import config.AnalysisConfig;
import connect.network.base.joggle.INetSender;
import encryption.AESReceiver;
import encryption.AESSender;
import log.LogDog;

import javax.net.ssl.SSLEngine;
import java.nio.channels.SocketChannel;

public class ConnectRemoteProxyServer extends AbsConnectClient {

    public ConnectRemoteProxyServer(INetSender localSender, String host, int port) {
        setAddress(host, port, false);
        boolean isEnableRSA = AnalysisConfig.getInstance().getBooleanValue("enableRSA");
        if (isEnableRSA) {
            setReceive(new AESReceiver(localSender));
            setSender(new AESSender());
        } else {
            setReceive(new RemoteRequestReceive(localSender));
            setSender(new CacheNioSender());
        }
    }

    @Override
    protected void onConnectCompleteChannel(boolean isConnect, SocketChannel channel, SSLEngine sslEngine) {
        if (isConnect) {
            getSender().setChannel(channel);
        }
        LogDog.d("==> connect Remote Proxy Server ing ... status = " + isConnect);
    }

}
