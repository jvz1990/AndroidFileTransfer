package com.johanvz.SEC;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import java.security.*;
import java.util.Arrays;

import static javax.xml.bind.DatatypeConverter.printHexBinary;
/**
 * Created by j on 3/07/2017.
 * Please note this will only work with key length of 64 bytes (from ECDH class)
 */
public class AES {

    // AES-GCM parameters
    private static final int GCM_NONCE_LENGTH = 12; // in bytes
    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String SEC_PROVIDER = "SunJCE";

    //Cipher machines
    private Cipher encryptor, decryptor;

    public AES(byte[] key) {
        try {
            Key secretKey = generateKey(key);
            final byte[] nonce = provideNonce(key);
            byte[] aad = provideAAD(key);

            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, nonce);

            encryptor = Cipher.getInstance(ALGORITHM, SEC_PROVIDER);
            decryptor = Cipher.getInstance(ALGORITHM, SEC_PROVIDER);

            encryptor.init(Cipher.ENCRYPT_MODE, secretKey, spec);
            encryptor.updateAAD(aad);

            decryptor.init(Cipher.DECRYPT_MODE, secretKey, spec);
            decryptor.updateAAD(aad);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public byte[] encrypt(byte[] message) throws BadPaddingException, IllegalBlockSizeException {
        return encryptor.doFinal(message);
    }

    public byte[] decrypt(byte[] message) throws BadPaddingException, IllegalBlockSizeException {
        return decryptor.doFinal(message);
    }

    private static byte[] provideNonce(byte[] key) {
        return Arrays.copyOfRange(key, key.length - GCM_NONCE_LENGTH, key.length);
    }

    private static byte[] provideAAD(byte[] key) {
        return Arrays.copyOfRange(key, 16, 48);
    }

    private static Key generateKey(byte[] key) {
        return new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
    }

    public static void main(String[] args) throws Exception {

        // Sample ECDH key
        String testKey = "CF6C722355EF1C6821A5A110BFB4F2E5F8A26E86AD944ABE63A1067997D6C04B";
        byte[] bTestKey = testKey.getBytes();

        AES aes = new AES(bTestKey);
        byte[] ciphered = aes.encrypt("I like bananas".getBytes());
        System.out.println(printHexBinary(ciphered));

        byte[] deciphered = aes.decrypt(ciphered);
        System.out.println(new String(deciphered));


    }
}