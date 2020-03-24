package encryption;

import connect.RemoteRequestReceive;
import connect.network.base.joggle.INetSender;

import java.io.IOException;

public abstract class DecryptionReceiver extends RemoteRequestReceive {

    public DecryptionReceiver(INetSender localTarget) {
        super(localTarget);
    }

    @Override
    protected void onResponse(byte[] response) throws IOException {
        response = onDecrypt(response);
        localTarget.sendData(response);
    }

    protected abstract byte[] onDecrypt(byte[] src);
}
