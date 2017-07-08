package com.johanvz.UDP;

import com.johanvz.TCP.Master;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.*;
import java.util.Observable;

import static com.johanvz.Utils.Consts.*;

/**
 * Created by j on 26/06/2017.
 */
public class Listener extends Observable implements Runnable {

    public static boolean keepAlive = true;
    private static DatagramSocket datagramSocket = null;

    private Listener() {
        initSocket();
    }

    private static void initSocket() {
        if (datagramSocket == null) {
            try {
                datagramSocket = new DatagramSocket(UDP_PORT, InetAddress.getByName("0.0.0.0"));
                datagramSocket.setBroadcast(true);
                //datagramSocket.setSoTimeout(TIME_OUT);
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
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

                byte[] rawPacket = new byte[1024];

                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rawPacket);
                DatagramPacket datagramPacket = new DatagramPacket(rawPacket, rawPacket.length);

                while (keepAlive) {

                    datagramSocket.receive(datagramPacket);
                    ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                    Packet packet = (Packet) objectInputStream.readObject();
                    packet.setInetAddress(datagramPacket.getAddress());
                    byteArrayInputStream.reset();

                    if(packet.getTCPport() != Master.getPortNo()) {
                        setChanged();
                        notifyObservers(packet);
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace(); //expected
            } finally {
                if (datagramSocket != null) {
                    datagramSocket.close();
                    datagramSocket = null;
                }
            }
        }
    }

    public static Listener getInstance() {
        return ListenerHolder.INSTANCE;
    }

    public static Thread getThread() {
        return ListenerHolder.LISTENER;
    }

    public static void init() {
        new ListenerHolder();
    }

    private static class ListenerHolder {
        private static final Listener INSTANCE = new Listener();
        private static final Thread LISTENER = new Thread(INSTANCE);

        private ListenerHolder() {
            if (!LISTENER.isDaemon()) LISTENER.setDaemon(true);
            if (!LISTENER.isAlive()) LISTENER.start();
        }
    }

}
