package com.johanvz.UDP;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Created by j on 3/07/2017.
 */
public class Packet implements Serializable {
    private static final long serialVersionUID = -1068275843273170820L;
    private String machineName;
    private int TCPport;
    private byte[] publicKey;
    private InetAddress inetAddress;

    Packet(String machineName, int TCPport, byte[] publicKey) {
        this.machineName = machineName;
        this.TCPport = TCPport;
        this.publicKey = publicKey;
    }

    public String getMachineName() {
        return machineName;
    }

    public int getTCPport() {
        return TCPport;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public InetAddress getInetAddress() {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
    }
}
