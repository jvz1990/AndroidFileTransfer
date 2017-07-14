package com.johanvz.GUI.Tabs.DeviceList;

import com.johanvz.Components.Device;
import com.johanvz.SEC.ECDH;
import com.johanvz.TCP.Sender;
import com.johanvz.UDP.Listener;
import com.johanvz.UDP.Packet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by j on 12/07/2017.
 */
public class Devices extends JPanel implements ActionListener, Observer {
    private static boolean initialized = false;
    private static JList<Device> deviceJList;
    private static final DefaultListModel<Device> devices = new DefaultListModel<>();

    private static JButton sendFiles;

    private Devices() {
        super(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        deviceJList = new JList<>(devices);
        deviceJList.setCellRenderer(new DeviceCellRenderer());

        JScrollPane jScrollPane = new JScrollPane(deviceJList);
        this.add(jScrollPane);

        sendFiles = new JButton("Send to machine");
        sendFiles.addActionListener(this);

        this.add(sendFiles, BorderLayout.PAGE_END);

        initialized = true;
    }

    private void sendFile(String filePath, Device device, String parentDirectory) {
        try {
            File file = new File(filePath);
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if(files != null) {
                    if(files.length > 0) {
                        for (File newFile : files) {
                            sendFile(newFile.getCanonicalPath(), device, "\\".concat(file.getName()));
                        }
                    }
                }
            } else {
                new Sender(filePath, device, parentDirectory);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == sendFiles) {
            if (!deviceJList.isSelectionEmpty()) {
                Device device = devices.get(deviceJList.getSelectedIndex());
                for (int i = 0; i < devices.getSize(); i++) {
                    sendFile(devices.getElementAt(i).toString(), device, "");
                }
            }
        }
    }

    public static Devices getInstance() {
        if (!initialized) {
            new DevicesHolder();
        }
        return DevicesHolder.INSTANCE;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof Listener) {
            if (arg instanceof Packet) {
                Packet packet = (Packet) arg;
                synchronized (devices) {
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getInetAddress().equals(packet.getInetAddress())) {
                            devices.get(i).setTCPport(packet.getTCPport());
                            devices.get(i).setLastTimeHeardFrom(System.currentTimeMillis());
                            return;
                        }
                    }
                    devices.addElement(new Device(packet.getMachineName(), packet.getTCPport(), packet.getPublicKey(), packet.getInetAddress()));
                    ECDH.addPublicKey(packet.getPublicKey(), packet.getInetAddress());
                }

            }
        }
    }

    private final static class DevicesHolder {
        private static final Devices INSTANCE = new Devices();

        private DevicesHolder() {
        }
    }
}
