package com.johanvz.GUI.Tabs;

import com.johanvz.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by j on 12/07/2017.
 */
public final class SEC extends JPanel implements ActionListener {

    private static boolean initilized = false;
    private static JCheckBox enabledSecurity;

    public SEC() {
        super(new BorderLayout());
        if(initilized) return;
        this.setBorder(BorderFactory.createLineBorder(Color.black));
        this.setOpaque(true);

        enabledSecurity = new JCheckBox("Enable Security");
        enabledSecurity.addActionListener(this);

        this.add(enabledSecurity, BorderLayout.PAGE_START);
        this.add(new JLabel("Please note: encryption is very resource consuming, please limit to < 100MB"), BorderLayout.CENTER);

        initilized = true;
    }

    private static void init() {
        new SECholder();
    }

    public static SEC getInstance() {
        if(!initilized) init();
        return SECholder.INSTANCE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == enabledSecurity) {
            Main.enableSec = enabledSecurity.isSelected();
        }
    }

    private final static class SECholder {
        private static final SEC INSTANCE = new SEC();

        private SECholder() {
        }
    }
}
