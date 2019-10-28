package encryption;

public class RSASender extends EncryptionSender {

    @Override
    byte[] onEncrypt(byte[] src) {
        return RSADataEnvoy.getInstance().publicEncrypt(src);
    }
}
