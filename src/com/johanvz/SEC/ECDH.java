package com.johanvz.SEC;

import javax.crypto.KeyAgreement;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by j on 3/07/2017.
 */
public final class ECDH {

    private static boolean initialized = false;

    private static KeyPair keyPair;
    private static byte[] publicKey;

    private static KeyFactory keyFactory;
    private static Hashtable<InetAddress, byte[]> sharedKeys;


    private ECDH() {
        if(!initialized) init();
    }

    private static synchronized void initialize() {
        sharedKeys = new Hashtable<>();
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
            keyPairGenerator.initialize(256);
            keyPair = keyPairGenerator.generateKeyPair();
            publicKey = keyPair.getPublic().getEncoded();

            keyFactory = KeyFactory.getInstance("EC");
            initialized = true;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            initialized = false;
        }
    }

    public static synchronized void removePublicKey(InetAddress key) {
        sharedKeys.remove(key);
    }

    public static synchronized void addPublicKey(byte[] otherKey, InetAddress IPAddress) {
        if(!initialized) init();
        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(otherKey);
            PublicKey otherPublicKey = keyFactory.generatePublic(keySpec);

            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(otherPublicKey, true);

            byte[] sharedSecret = keyAgreement.generateSecret();

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(sharedSecret);

            List<ByteBuffer> keys = Arrays.asList(ByteBuffer.wrap(publicKey), ByteBuffer.wrap(otherKey));
            Collections.sort(keys);
            messageDigest.update(keys.get(0));
            messageDigest.update(keys.get(1));

            sharedKeys.put(IPAddress, messageDigest.digest());
            //System.out.println(DatatypeConverter.printHexBinary(sharedKeys.get(IPAddress)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static byte[] getPublicKey() {
        if(!initialized) initialize();
        return publicKey;
    }


    public static void init() {
        new ECDHHolder();
    }

    private static final class ECDHHolder {
        private static final ECDH INSTANCE = new ECDH();
        private ECDHHolder() {
            if(!initialized) {
                init();
            }
        }
    }

    public static void main(String[] args) {
        new ECDHHolder();
    }
}
