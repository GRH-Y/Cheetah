package encryption;

import connect.network.base.joggle.INetSender;

public class AESReceiver extends DecryptionReceiver {

    public AESReceiver(INetSender localTarget) {
        super(localTarget);
    }

    @Override
    protected byte[] onDecrypt(byte[] src) {
        return AESDataEnvoy.getInstance().decrypt(src);
    }
}
