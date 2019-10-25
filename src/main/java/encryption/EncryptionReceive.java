package encryption;


import connect.network.nio.NioReceive;
import util.IoEnvoy;
import util.ThreadAnnotation;

public abstract class EncryptionReceive extends NioReceive {

    @Override
    protected void onRead() {
        try {
            byte[] data = IoEnvoy.tryRead(channel);
            onDecrypt(data);
            if (data != null) {
                ThreadAnnotation.disposeMessage(this.mReceiveMethodName, this.mReceive, data);
            }
//            else {
//                NioHPCClientFactory.getFactory().removeTask(nioClientTask);
//            }
        } catch (Exception e) {
//            NioHPCClientFactory.getFactory().removeTask(nioClientTask);
        }
    }

    abstract byte[] onDecrypt(byte[] encrypt);
}
