package encryption;


import connect.RequestSender;

/**
 * 加密发送者
 */
public abstract class EncryptionSender extends RequestSender {

    @Override
    public void sendData(byte[] data) {
        super.sendData(onEncrypt(data));
    }

    @Override
    public void sendDataNow(byte[] data) {
        super.sendDataNow(onEncrypt(data));
    }


    abstract byte[] onEncrypt(byte[] src);
}
