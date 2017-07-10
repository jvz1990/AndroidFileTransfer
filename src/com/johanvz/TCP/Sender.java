package com.johanvz.TCP;

import com.johanvz.Components.Device;
import com.johanvz.SEC.AES;
import com.johanvz.SEC.ECDH;
import com.johanvz.Utils.Consts;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;

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
    }

    @Override
    public void run() {

        AES encryptor = new AES(ECDH.getSharedKeys().get(device.getInetAddress()), Cipher.ENCRYPT_MODE);

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

            //DataOutputStream dataOutputStream = new DataOutputStream(transferSocket.getOutputStream());
            byte[] buffer = new byte[Consts.PACKET_SIZE];

            CipherOutputStream cipherOutputStream = new CipherOutputStream(transferSocket.getOutputStream(), encryptor.getCipher());

            while(fileInputStream.read(buffer) > 0) {
                cipherOutputStream.write(buffer);
                //cipherOutputStream.flush();
                //dataOutputStream.write(buffer);
            }

            cipherOutputStream.flush();
            cipherOutputStream.close();
            cipherOutputStream = null;
            /*dataOutputStream.flush();
            dataOutputStream.close();
            dataOutputStream = null;*/

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
