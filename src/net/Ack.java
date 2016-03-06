package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import ui.Params;

public class Ack implements Runnable {
    
    private final byte[] buffer = new byte[Params.LONG_BYTE_SIZE];
    private final DatagramSocket dSock;
    private final Protocol p;
    private final InetAddress ia;
    private final int port;
    
    private volatile boolean isRunning;
    
    public Ack(Protocol pp, InetAddress ia, int port) throws SocketException{
        p = pp;
        if(ia == null)
            dSock = new DatagramSocket(port);
        else
            dSock = new DatagramSocket();  
        isRunning = false;
        this.ia = ia;
        this.port = port;
    }    
    public synchronized void sendAck(long num){
        DatagramPacket dPack = new DatagramPacket(Params.int2Byte(num, buffer, 0), buffer.length, ia, port);
        try{
            dSock.send(dPack);
        }catch(IOException e) {
            System.out.println("sudden io error");
        }
    } 
    @Override
    public void run() {
       DatagramPacket dPack = new DatagramPacket(buffer, buffer.length);
       isRunning = true;  
        while(isRunning){
            try {
                dSock.receive(dPack);
                 p.addAck(Params.byte2Int(buffer, 0));
            }catch(IOException e) {}
        }
    }
    public void stop(){
        if(isRunning){
            isRunning = false;
            dSock.close();
        }
    }
}