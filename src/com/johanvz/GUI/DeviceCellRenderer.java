package com.johanvz.GUI;

import com.johanvz.Components.Device;

import javax.swing.*;
import java.awt.*;

/**
 * Created by j on 6/07/2017.
 */
public class DeviceCellRenderer extends JLabel implements ListCellRenderer {

    private static final Color HIGHLIGHT_COLOR = new Color(0, 0, 128);

    public DeviceCellRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        if(value instanceof Device) {
            Device device = (Device) value;
            setText("Machine: " + device.getMachineName() + " IP: " + device.getInetAddress().toString().replace("/","") + ":" + device.getTCPport());

            if (isSelected) {
                setBackground(HIGHLIGHT_COLOR);
                setForeground(Color.white);
            } else {
                setBackground(Color.white);
                setForeground(Color.black);
            }

            return this;
        } else return null;

    }
}
