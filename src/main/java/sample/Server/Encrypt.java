/*This file contains the class which uses AES encryption with a hard-coded key to encode and decode
 * message sent on a network
 * File: Encrypt.java
 * Author: Serdiuk Andrii
 * */


package Server;


import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class Encrypt{

    private static final String myEncryptionKey = "ThisIsFoundation";
    private final Cipher cipher = Cipher.getInstance("AES");
    private boolean inDecodeMode = false;
    //Avoiding unnecessary setup of the cipher which can be expensive
    private static Key secretKey;

    private static Key generateKey() {
        byte[] keyAsBytes;
        keyAsBytes = myEncryptionKey.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyAsBytes, "AES");
    }

    public Encrypt() throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        secretKey = generateKey();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
    }

    /**Creates an Classes.Encrypt object with the specified mode
     * If setDecryptMode is set to true, then the cipher is initialised in {@code Cipher.DECRYPT_MODE}
     * Otherwise the cipher is initialised in {@code Cipher.ENCRYPT_MODE}
     */
    public Encrypt(boolean setDecryptMode) throws InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        secretKey = generateKey();
        if (setDecryptMode)
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
        else
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        inDecodeMode = setDecryptMode;
    }

    public byte[] encode(byte[] array) throws Exception {
        if(inDecodeMode) {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            inDecodeMode = false;
        }
        return cipher.doFinal(array);
    }

    public byte[] decode(byte[] array) throws Exception {
        if(!inDecodeMode) {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            inDecodeMode = true;
        }
        return cipher.doFinal(array);
    }


}
