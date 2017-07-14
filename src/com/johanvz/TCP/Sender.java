package com.johanvz.TCP;

import com.johanvz.Components.Device;
import com.johanvz.Main;
import com.johanvz.SEC.AES;
import com.johanvz.SEC.ECDH;
import com.johanvz.Utils.Consts;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import java.io.*;
import java.net.Socket;

/**
 * Created by j on 30/06/2017.
 * Class to send a file to device.
 */
public class Sender implements Runnable {

    private String fileToSend;
    private Device device;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;
    private Packet packet;
    private boolean enabledSec = false;
    private String folderStructure = "";

    public Sender(String fileToSend, Device device, String parentDirectory) {
        this.fileToSend = fileToSend;
        this.device = device;
        this.enabledSec = Main.enableSec;
        this.folderStructure = parentDirectory;
        new Thread(this).start();
    }

    @Override
    public void run() {
        packet = new Packet();
        try {
            FileInputStream fileInputStream = new FileInputStream(fileToSend);
            packet.setFileSize((int) fileInputStream.getChannel().size());
            if(folderStructure.length() > 0) {
                packet.setFileName(folderStructure.concat("\\").concat(new File(fileToSend).getName()));
            } else {
                packet.setFileName((new File(fileToSend)).getName());
            }
            packet.setReady(false);
            packet.setPortNo(0);

            Socket initialSocket = new Socket(device.getInetAddress(), device.getTCPport());
            objectOutputStream = new ObjectOutputStream(initialSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(initialSocket.getInputStream());


            Socket transferSocket = getTransferSocket();
            objectInputStream.close();
            objectInputStream = null;
            objectOutputStream.close();
            objectOutputStream = null;
            initialSocket.close();

            byte[] buffer = new byte[Consts.PACKET_SIZE];

            if (enabledSec) {
                AES encryptor = new AES(ECDH.getSharedKeys().get(device.getInetAddress()), Cipher.ENCRYPT_MODE);
                CipherOutputStream cipherOutputStream = new CipherOutputStream(transferSocket.getOutputStream(), encryptor.getCipher());
                while (fileInputStream.read(buffer) > 0) {
                    cipherOutputStream.write(buffer);
                }
                cipherOutputStream.flush();
                cipherOutputStream.close();
            } else {
                DataOutputStream dataOutputStream = new DataOutputStream(transferSocket.getOutputStream());
                while (fileInputStream.read(buffer) > 0) {
                    dataOutputStream.write(buffer);
                }
                dataOutputStream.flush();
                dataOutputStream.close();
            }

            transferSocket.close();

        } catch (IOException e) {
            e.printStackTrace();
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

        while (socket == null) {
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
