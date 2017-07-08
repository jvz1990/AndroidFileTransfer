package com.johanvz.GUI;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.util.List;

/**
 * Created by j on 5/07/2017.
 */
public class ListTransferHandler extends TransferHandler {

    private JList list;

    public ListTransferHandler(JList list) {
        this.list = list;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport info) {
        // we only import FileList
        return info.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        // Check for FileList flavor
        if (!info.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            displayDropLocation("List doesn't accept a drop of this type.");
            return false;
        }

        // Get the fileList that is being dropped.
        Transferable t = info.getTransferable();
        List<File> data;
        try {
            data = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
        } catch (Exception e) {
            return false;
        }
        DefaultListModel model = (DefaultListModel) list.getModel();
        for (Object file : data) {
            model.addElement((File) file);
        }
        return true;
    }

    private void displayDropLocation(String string) {
        System.out.println(string);
    }
}
