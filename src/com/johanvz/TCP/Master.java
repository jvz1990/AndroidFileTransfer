package com.johanvz.TCP;

import com.johanvz.Main;
import com.johanvz.Utils.Consts;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by j on 3/07/2017.
 */
public final class Master implements Runnable {

    private static int portNo = 0;
    private static boolean initialzed = false;
    private static ServerSocket serverSocket = null;
    private static Socket socket = null;
    private static String hostname;
    private static InetAddress inetAddress;
    public static boolean keepAlive = true;

    private static synchronized void initialize() {
        if(initialzed) return;
        initialzed = true;
        portNo = getNewPort();
        //Prevent port clashes
        new Thread(() -> {
            while (keepAlive) {
                try {
                    Thread.sleep(Consts.POLL_TIME * 2 + Consts.random.nextInt(Consts.POLL_TIME * 4));
                    portNo = getNewPort();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        try {
            hostname = InetAddress.getLocalHost().getHostName();
            inetAddress = InetAddress.getLocalHost();
            serverSocket = new ServerSocket(portNo);
        } catch (IOException e) {
            e.printStackTrace();
            initialzed = false;
        }

    }

    private Master() {
        if (!initialzed) initialize();
    }

    private static int getNewPort() {
        int testPort = portNo;
        boolean collision = false;
        if(testPort == 0) {
            return Consts.random.nextInt(10000) + 20000;
        }
/*        synchronized (Main.getDevices()) {
            for (int i = 0; i < Main.getDevices().size(); i++) {
                if (testPort == Main.getDevices().get(i).getTCPport()) {
                    i = 0;
                    testPort = Consts.random.nextInt(10000) + 20000;
                    collision = true;
                }
            }
        }*/
        if(!collision) return testPort;
        initialzed = false;
        try {
            if(socket != null) {
                if(socket.isBound()) socket.close();
            }
            if(serverSocket != null) {
                if(serverSocket.isBound()) serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return testPort;
    }

    @Override
    public void run() {

        while (keepAlive) {

            ObjectInputStream objectInputStream = null;
            ObjectOutputStream objectOutputStream = null;
            Packet packet;

            while (keepAlive) {
                if (!serverSocket.isClosed()) {
                    initialize();
                }
                try {
                    socket = serverSocket.accept();
                    objectInputStream = new ObjectInputStream(socket.getInputStream());
                    objectOutputStream = new ObjectOutputStream(socket.getOutputStream());

                    Object o = objectInputStream.readObject();

                    if (o instanceof Packet) {
                        packet = (Packet) o;
                        ServerSocket newServerSocket = null;

                        while (!packet.isReady()) {
                            newServerSocket = getNewSocket();

                            packet.setPortNo(newServerSocket.getLocalPort());

                            objectOutputStream.writeObject(packet);
                            objectOutputStream.flush();

                            o = objectInputStream.readObject();
                            packet = (Packet) o;
                            if (!packet.isReady()) newServerSocket.close();
                        }
                        new Receiver(newServerSocket, packet.getFileSize(), packet.getFileName());
                    }

                    socket.close();

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        }

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ServerSocket getNewSocket() {
        ServerSocket serverSocket = null;
        while (serverSocket == null) {
            try {
                serverSocket = new ServerSocket(Consts.random.nextInt(10000) + 10000);
            } catch (IOException e) {
                e.printStackTrace();
                serverSocket = null;
            }
        }

        return serverSocket;
    }

    public static String getHostname() {
        if (!initialzed) initialize();
        return hostname;
    }

    public static int getPortNo() {
        if (!initialzed) initialize();
        return portNo;
    }

    public static synchronized void init() {
        if (!initialzed) initialize();
        new MasterHolder();
    }


    public static Thread getThread() {
        return MasterHolder.THREAD;
    }

    private static final class MasterHolder {
        private static final Master INSTANCE = new Master();
        private static final Thread THREAD = new Thread(INSTANCE);

        private MasterHolder() {
            if (!THREAD.isDaemon()) THREAD.setDaemon(true);
            if (!THREAD.isAlive()) THREAD.start();
        }
    }
}
