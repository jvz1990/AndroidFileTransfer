package com.johanvz.TCP;

import com.johanvz.Components.Device;
import com.johanvz.Utils.Consts;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import java.io.*;
import java.net.Socket;

/**
 * Created by j on 30/06/2017.
 */
public class Sender implements Runnable {

    private Socket initialSocket, transferSocket;
    private String fileToSend;
    private int port;
    private Device device;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private FileInputStream fileInputStream;
    private Packet packet;

    public Sender(String fileToSend, Device device) {
        this.fileToSend = fileToSend;
        this.device = device;
        new Thread(this).start();
        System.out.println("Sender initialised");
    }

    @Override
    public void run() {

        System.out.println("Sender running");

        packet = new Packet();
        try {
            fileInputStream = new FileInputStream(fileToSend);
            packet.setFileSize((int) fileInputStream.getChannel().size());
            packet.setFileName((new File(fileToSend)).getName());
            packet.setReady(false);
            packet.setPortNo(0);

            initialSocket = new Socket(device.getInetAddress(), device.getTCPport());
            objectOutputStream = new ObjectOutputStream(initialSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(initialSocket.getInputStream());

            System.out.println("Send initial packet");

            transferSocket = getTransferSocket();
            objectInputStream.close();
            objectInputStream = null;
            objectOutputStream.close();
            objectOutputStream = null;
            initialSocket.close();
            initialSocket = null;

            DataOutputStream dataOutputStream = new DataOutputStream(transferSocket.getOutputStream());
            byte[] buffer = new byte[Consts.PACKET_SIZE];

            while(fileInputStream.read(buffer) > 0) {
                dataOutputStream.write(buffer);
            }

            dataOutputStream.flush();
            dataOutputStream.close();
            dataOutputStream = null;

            transferSocket.close();
            transferSocket = null;

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Major error");
        }

    }

    private Socket getTransferSocket() {
        Socket socket = null;
        Object o;

        try {
            objectOutputStream.writeObject(packet);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(socket == null) {
            try {
                o = objectInputStream.readObject();
                packet = (Packet) o;
                socket = new Socket(device.getInetAddress(), packet.getPortNo());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                socket = null;
                packet.setReady(false);

                try {
                    objectOutputStream.writeObject(packet);
                    objectOutputStream.flush();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        packet.setReady(true);

        try {
            objectOutputStream.writeObject(packet);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return socket;
    }

}
