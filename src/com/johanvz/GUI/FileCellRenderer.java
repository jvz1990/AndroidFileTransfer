package com.johanvz.GUI;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.io.File;

/**
 * Created by j on 5/07/2017.
 */
public class FileCellRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list,
                                                  Object value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        Component component = super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);

        if (component instanceof JLabel && value instanceof File) {
            JLabel jLabel = (JLabel) component;
            File file = (File) value;
            jLabel.setIcon(FileSystemView.getFileSystemView().getSystemIcon(file));
            jLabel.setText(file.getName());
            jLabel.setToolTipText(file.getAbsolutePath());
        }

        return component;
    }
}
