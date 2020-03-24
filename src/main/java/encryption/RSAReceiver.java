package encryption;

import connect.network.base.joggle.INetSender;

public class RSAReceiver extends DecryptionReceiver {

    public RSAReceiver(INetSender localTarget) {
        super(localTarget);
    }

    @Override
    protected byte[] onDecrypt(byte[] src) {
        return RSADataEnvoy.getInstance().superCipher(src, true, false);
    }
}
