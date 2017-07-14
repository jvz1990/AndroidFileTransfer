package com.johanvz.Components;

import java.net.InetAddress;

/**
 * Created by j on 6/07/2017.
 */
public class Device {
    private String machineName;
    private int TCPport;
    private byte[] publicKey;
    private InetAddress inetAddress;
    private double lastTimeHeardFrom = System.currentTimeMillis();

    public Device(String machineName, int TCPport, byte[] publicKey, InetAddress inetAddress) {
        this.machineName = machineName;
        this.TCPport = TCPport;
        this.publicKey = publicKey;
        this.inetAddress = inetAddress;
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

    public double getLastTimeHeardFrom() {
        return lastTimeHeardFrom;
    }

    public void setLastTimeHeardFrom(double lastTimeHeardFrom) {
        this.lastTimeHeardFrom = lastTimeHeardFrom;
    }

    public void setTCPport(int TCPport) {
        this.TCPport = TCPport;
    }
}
