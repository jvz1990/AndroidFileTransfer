package com.johanvz.Utils;

import java.util.Random;

/**
 * Created by j on 23/06/2017.
 */
public interface Consts {
    Random random = new Random(System.nanoTime());
    int TIME_OUT = 2000; //ms
    int UDP_PORT = 18641;
    int POLL_TIME = 1000; //Broadcast packet period
    int PACKET_SIZE = 1500; //730 buf
}
