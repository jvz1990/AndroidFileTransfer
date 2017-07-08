package com.johanvz.TCP;

import com.johanvz.Main;
import com.johanvz.Utils.Consts;
import com.johanvz.Utils.PlatformUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * Created by j on 29/06/2017.
 */
public class Receiver implements Runnable {

    private ServerSocket serverSocket = null;
    private String fileName;
    private int fileSize;

    public Receiver(ServerSocket serverSocket, int fileSize, String fileName) {
        this.serverSocket = serverSocket;
        this.fileSize = fileSize;
        this.fileName = fileName;

        new Thread(this).start();
    }

    @Override
    public void run() {

        try {
            Socket socket = null;
            InputStream inputStream = null;
            DataInputStream dataInputStream = null;
            FileOutputStream fileOutputStream = null;
            byte[] buffer = new byte[Consts.PACKET_SIZE];
            int bytesRead, current = 0, remaining;

            socket = serverSocket.accept();
            inputStream = socket.getInputStream();

            dataInputStream = new DataInputStream(inputStream);

            if(PlatformUtils.isWindows()) {
                fileOutputStream = new FileOutputStream(Main.FilePath.concat("\\").concat(fileName));
            } else {
                fileOutputStream = new FileOutputStream(Main.FilePath.concat("/").concat(fileName));
            }


            remaining = fileSize;
            while ((bytesRead = dataInputStream.read(buffer, 0, Math.min(buffer.length, remaining))) > 0) {
                current += bytesRead;
                remaining -= bytesRead;
                fileOutputStream.write(buffer, 0, bytesRead);
                System.out.println("Total read = " + current + " percent = " + ((current / (double) fileSize) * 100.0) + "%");
            }

            fileOutputStream.close();

            dataInputStream.close();
            inputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
