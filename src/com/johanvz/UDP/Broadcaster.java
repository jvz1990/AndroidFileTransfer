package com.johanvz.UDP;

import com.johanvz.SEC.ECDH;
import com.johanvz.TCP.Master;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Enumeration;

import static com.johanvz.Utils.Consts.*;

/**
 * Created by j on 27/06/2017.
 */
public class Broadcaster implements Runnable {

    public static boolean keepAlive = true;
    private static DatagramSocket datagramSocket = null;

    private static InetAddress SUBNET_255 = null;

    private Broadcaster() {
        initSocket();
    }

    private static void initSocket() {
        if (datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket();
                datagramSocket.setBroadcast(true);
                datagramSocket.setSoTimeout(TIME_OUT);
            } catch (SocketException e) {
                //e.printStackTrace();
            }
        }

        if (SUBNET_255 == null) {
            try {
                SUBNET_255 = InetAddress.getByName("255.255.255.255");
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (keepAlive) {

            try {

                if (datagramSocket == null) {
                    initSocket();
                }

                byte[] rawPacket = updatePacket();
                DatagramPacket datagramPacket = new DatagramPacket(rawPacket, rawPacket.length, SUBNET_255, UDP_PORT);
                Packet packet = new Packet(Master.getHostname(), Master.getPortNo(), ECDH.getPublicKey());

                datagramPacket.setData(rawPacket);

                while (keepAlive) {

                    if(packet.getTCPport() != Master.getPortNo()) {
                        rawPacket = updatePacket();
                        packet = new Packet(Master.getHostname(), Master.getPortNo(), ECDH.getPublicKey());
                        datagramPacket.setData(rawPacket);
                    }

                    datagramPacket.setAddress(SUBNET_255);

                    datagramSocket.send(datagramPacket);
                    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                    while (interfaces.hasMoreElements()) {
                        NetworkInterface networkInterface = interfaces.nextElement();

                        if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
                                InetAddress broadcast = interfaceAddress.getBroadcast();
                                if (broadcast != null) {
                                    datagramPacket.setAddress(broadcast);
                                    datagramSocket.send(datagramPacket);
                                    //System.out.println(getClass().getName() + ">>> Request packet sent to: " + broadcast.getHostAddress() + "; Interface: " + networkInterface.getDisplayName());
                                }
                            }
                        }
                    }

                    Thread.sleep(POLL_TIME);
                }


            } catch (Exception e) {

            } finally {
                if (datagramSocket != null) {
                    datagramSocket.close();
                }
                datagramSocket = null;
            }


        }
    }

    private byte[] updatePacket() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            Packet packet = new Packet(Master.getHostname(), Master.getPortNo(), ECDH.getPublicKey());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);

            objectOutputStream.writeObject(packet);
            objectOutputStream.flush();
            byteArrayOutputStream.flush();

            byte[] toReturn = byteArrayOutputStream.toByteArray();
            objectOutputStream.close();
            byteArrayOutputStream.close();

            return toReturn;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static Broadcaster getInstance() {
        return BroadcasterHolder.INSTANCE;
    }

    public static Thread getThread() {
        return BroadcasterHolder.BROADCASTER;
    }

    public static void init() {
        new BroadcasterHolder();
    }

    private static class BroadcasterHolder {
        private static final Broadcaster INSTANCE = new Broadcaster();
        private static final Thread BROADCASTER = new Thread(INSTANCE);

        private BroadcasterHolder() {
            if (!BROADCASTER.isDaemon()) BROADCASTER.setDaemon(true);
            if (!BROADCASTER.isAlive()) BROADCASTER.start();
        }
    }

    public static void main(String[] args) {
        Listener.init();
        init();
        try {
            Thread.sleep(200000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
