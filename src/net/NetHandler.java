package net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class NetHandler implements Runnable {
    private DatagramSocket dSock;
    private DatagramPacket dPack;
    private volatile boolean isRunning;
    private final Protocol p;
    private InetAddress ia;
    private int port;
    
    public NetHandler(Protocol pp){
        isRunning = false; 
        p = pp;
    }    
    public void init(int port, InetAddress ia, String filePath) throws SocketException{   
        if(isRunning){
            throw new IllegalStateException();
        }
        this.port = port;
        this.ia = ia;
        if(ia == null)
            dSock = new DatagramSocket(port);
        else
            dSock = new DatagramSocket();  
    }    
    @Override
    public void run() {
        isRunning = true;   
        try{               
            while(isRunning){
                dPack = new DatagramPacket(p.tempBuffer, p.tempBuffer.length);
                dSock.receive(dPack);
                p.handleInc(dPack.getLength());  
            }
        }catch(IOException e){ }
        finally {
            stop();
        }
    }    
    public void signal(int len) throws IOException {
        dPack = new DatagramPacket(p.tempBuffer, len, ia, port);
        dSock.send(dPack);     
    }    
    public void stop(){   
        if(isRunning){
            isRunning = false;
            if(dSock != null)
                dSock.close();    
        }        
    }
}
