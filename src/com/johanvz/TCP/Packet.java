package com.johanvz.TCP;

import java.io.Serializable;

/**
 * Created by j on 29/06/2017.
 */
class Packet implements Serializable{
    private static final long serialVersionUID = -5026640405965040357L;
    private String fileName;
    private int fileSize;
    private boolean ready = false;
    private int portNo;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public int getPortNo() {
        return portNo;
    }

    public void setPortNo(int portNo) {
        this.portNo = portNo;
    }
}