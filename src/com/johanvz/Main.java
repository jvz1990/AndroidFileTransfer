package com.johanvz;

import com.johanvz.Components.Device;
import com.johanvz.GUI.*;
import com.johanvz.GUI.Tabs.DeviceList.DeviceCellRenderer;
import com.johanvz.GUI.Tabs.DeviceList.Devices;
import com.johanvz.GUI.Tabs.SEC;
import com.johanvz.SEC.ECDH;
import com.johanvz.TCP.Master;
import com.johanvz.TCP.Sender;
import com.johanvz.UDP.Broadcaster;
import com.johanvz.UDP.Listener;
import com.johanvz.UDP.Packet;
import com.johanvz.Utils.Consts;
import com.johanvz.Utils.CreateDirectory;
import com.johanvz.Utils.ProjectPath;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Observable;
import java.util.Observer;

public final class Main extends JPanel implements ActionListener, Observer, Runnable {

    private JButton clearAll, sendFiles;
    private DefaultListModel listModel;

    private static ArrayDeque<Thread> threadList = new ArrayDeque<>();
    public static String FilePath;

    //private static final DefaultListModel<Device> devices = new DefaultListModel<>();
    //private JList deviceList;
    public static boolean enableSec = false;

    private Main() {
        super(new BorderLayout());

        FilePath = ProjectPath.getFileLocation(Main.class).concat("\\Downloads");
        if (FilePath.contains("Main.jar\\")) FilePath = FilePath.replace("Main.jar\\", "");

        if (!CreateDirectory.CreatedDirectory(FilePath)) {
            System.out.println("Could not create directory");
        }

        // ---- GUI -----

        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setMultiSelectionEnabled(true);
        jFileChooser.setDragEnabled(true);
        jFileChooser.setControlButtonsAreShown(false);
        jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jFileChooser.addActionListener(this);

        JPanel fcPanel = new JPanel(new BorderLayout());
        fcPanel.add(jFileChooser, BorderLayout.CENTER);

        clearAll = new JButton("Clear All");
        clearAll.addActionListener(this);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        buttonPanel.add(clearAll, BorderLayout.LINE_END);

        JPanel leftUpperPanel = new JPanel(new BorderLayout());
        leftUpperPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leftUpperPanel.add(fcPanel, BorderLayout.CENTER);
        leftUpperPanel.add(buttonPanel, BorderLayout.PAGE_END);

        JScrollPane leftLowerPanel = new JScrollPane();
        leftLowerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        listModel = new DefaultListModel();
        JList dropZone = new JList(listModel);
        dropZone.setCellRenderer(new FileCellRenderer());
        dropZone.setTransferHandler(new ListTransferHandler(dropZone));
        dropZone.setDragEnabled(true);
        dropZone.setDropMode(javax.swing.DropMode.INSERT);
        dropZone.setBorder(new TitledBorder("Drag files into here"));
        leftLowerPanel.setViewportView(new JScrollPane(dropZone));

        JSplitPane lhs = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftUpperPanel, leftLowerPanel);
        lhs.setDividerLocation(400);
        lhs.setPreferredSize(new Dimension(480, 650));

        JTabbedPane jTabbedPane = new JTabbedPane();
        jTabbedPane.addTab("Files", lhs);

        // --- tab 2 ----
        jTabbedPane.add("Devices", Devices.getInstance());

        // --- tab 3 ----
        jTabbedPane.addTab("Settings", SEC.getInstance());

        add(jTabbedPane);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == clearAll) {
            listModel.clear();
        } else {
            System.out.println(e.getSource().toString());
        }
    }

    private static void createAndShowGUI() {
        JFrame.setDefaultLookAndFeelDecorated(true);
        boolean foundWinUI = false;
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    foundWinUI = true;
                    break;
                }
            }
            if (!foundWinUI) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame jFrame = new JFrame("File Transfer");
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        new MainHolder();
        jFrame.add(Main.getInstance());
        jFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                while (!threadList.isEmpty()) {
                    threadList.poll().interrupt();
                }
                System.exit(0);
            }
        });

        getInstance().setOpaque(true);
        jFrame.setContentPane(getInstance());
        jFrame.pack();
        jFrame.setVisible(true);
    }


    private static void createNetworkHandlers() {
        Master.init();
        threadList.add(Master.getThread());

        Listener.init();
        Listener.getInstance().addObserver(Main.getInstance());
        threadList.add(Listener.getThread());

        Broadcaster.init();
        threadList.add(Broadcaster.getThread());

    }

    @Override
    public void update(Observable o, Object arg) {

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            UIManager.put("swing.boldMetal", Boolean.FALSE);
            createAndShowGUI();
            createNetworkHandlers();
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(Consts.POLL_TIME * 2 + Consts.random.nextInt(Consts.POLL_TIME * 4));
/*                synchronized (devices) {
                    for (int i = 0; i < devices.size(); i++) {
                        if (devices.get(i).getLastTimeHeardFrom() + (Consts.POLL_TIME * 4) < System.currentTimeMillis()) {
                            devices.remove(i);
                        }
                    }
                }*/
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
        }
    }

    public static Main getInstance() {
        return MainHolder.INSTANCE;
    }

    public static class MainHolder {
        private static final Main INSTANCE = new Main();
        private static final Thread Main_t = new Thread(INSTANCE);

        private MainHolder() {
            if (!Main_t.isDaemon()) Main_t.setDaemon(true);
            if (!Main_t.isAlive()) Main_t.start();
        }
    }
}
