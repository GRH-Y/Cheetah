package connect;

import connect.network.base.joggle.INetSender;
import connect.network.nio.NioReceiver;

import java.io.IOException;

public class RemoteRequestReceive extends NioReceiver<byte[]> {

    protected INetSender localTarget;

    public RemoteRequestReceive(INetSender localTarget) {
        super(null);
        if (localTarget == null) {
            throw new NullPointerException("localTarget is null !!!");
        }
        this.localTarget = localTarget;
    }


    @Override
    protected void onResponse(byte[] response) throws IOException {
        localTarget.sendData(response);
//        LogDog.d("==> response = " + new String(response));
    }

}
