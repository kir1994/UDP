package net;

import files.FileRead;
import files.FileWrite;
import java.io.IOException;
import java.net.InetAddress;
import ui.Params;

public class Protocol implements Runnable{
    
    public byte[] buffer;
    public byte[] tempBuffer;
    
    private final int frameSize;
    private final int packSize;
    private final boolean sendMode;    
    private final NetHandler nh;
    private final FileRead fr;
    private final FileWrite fw;
    private final Ack ack;
    
    private Thread t;
    private long mask;    
    public long currentPos = 0;
    
    private volatile long acks = 0;    
    private volatile int blockSize;
    private volatile long lastPos = -1;
    private volatile boolean isRunning = false;
    
    private final Object lock = new Object();
    
    public Protocol(int fSize, int pSize, boolean sender, InetAddress ia, int dPort, int ackPort, String file){   
        packSize = pSize * Params.BYTES_IN_KIB;
        buffer = new byte[packSize * fSize];
        tempBuffer = new byte[packSize + Params.LONG_BYTE_SIZE];
        frameSize = fSize;
        nh = new NetHandler(this);   
        sendMode = sender;
        try{
            if(sendMode){
                ack = new Ack(this, null, ackPort);
                fr = new FileRead(this, file);
                fw = null;
                nh.init(dPort, ia, file);
                (new Thread(fr)).start();
                (new Thread(ack)).start();  
            }else {
                ack = new Ack(this, ia, ackPort);
                fr = null;
                fw = new FileWrite(this, file);
                nh.init(dPort, null, file);
                (new Thread(fw)).start();
                (new Thread(nh)).start();
            }
        }catch(IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }    
    public void handleInc(int len) {      
        if(len < 8)
            return;
        long pos = Params.byte2Int(tempBuffer, 0);                       
        synchronized(lock){  
            if((pos >= currentPos) && (pos < (currentPos + frameSize)) && (blockSize != -2)){
                if((acks & (1 << (pos % frameSize))) == 0){         
                        fw.write(-1, -1);
                        System.arraycopy(tempBuffer, Params.LONG_BYTE_SIZE, buffer, (int)(pos - currentPos) * packSize, len - Params.LONG_BYTE_SIZE); 
                        acks |= 1 << (pos % frameSize);
                        blockSize += (len - Params.LONG_BYTE_SIZE);
                        if(len - Params.LONG_BYTE_SIZE < packSize && lastPos == -1)
                            lastPos = pos;             
                        lock.notify();
                    }
                ack.sendAck(pos);
            }
        }        
    }
    @Override
    public void run() {
        t = Thread.currentThread();
        isRunning = true;  
        mask = (1 << frameSize) - 1;
        try{
            if(sendMode)               
                runSend();
            else
                runReceive();
        }catch(InterruptedException | IOException e){}
        finally{
            stop();
        }
    }    
    public void runSend() throws InterruptedException, IOException {
        int packLen;                  
        blockSize = -2;
        synchronized(lock){
            fr.read(0, buffer.length);  
            lock.wait();        
        }
        int size = (blockSize > 0) ? ((blockSize + packSize - 1) / packSize) : 1;
        mask >>= (frameSize - size);                
        while(isRunning){      
            synchronized(lock){                          
                if(acks == mask){  
                    if(blockSize < packSize * frameSize)
                        break;
                    blockSize = -2;
                    acks = 0;
                    System.out.println((currentPos + frameSize));
                    currentPos += frameSize;    
                    fr.read(0, buffer.length);   
                    lock.wait();
                    size = (blockSize > 0) ? ((blockSize + packSize - 1) / packSize) : 1;
                    if(size < frameSize && blockSize % packSize == 0)
                        size++;
                    if(size < frameSize)
                        mask >>= (frameSize - size);  
                    if(blockSize == -1)
                        blockSize = 0;  
                }
            }                           
            for(int i = 0; i < size; ++i){                       
               if((acks & (1 << i)) == 0){                           
                    packLen = (blockSize - (i + 1) * packSize >= 0) ? packSize : (blockSize % packSize);
                    Params.int2Byte(currentPos + i, tempBuffer, 0);
                    System.arraycopy(buffer, i * packSize, tempBuffer, Params.LONG_BYTE_SIZE, packLen);
                    nh.signal(packLen + Params.LONG_BYTE_SIZE);
               } 
            }
            Thread.sleep(1);
        }
    }
    public void runReceive() throws InterruptedException{
        blockSize = 0;
        synchronized(lock){
            while(isRunning){
                lock.wait();
                if(lastPos >= 0){
                    mask >>= frameSize - (lastPos % frameSize + 1);
                    lastPos = -2;
                }
                if(acks == mask){  
                    System.out.println(currentPos);
                    acks = 0;
                    currentPos += frameSize;  
                    fw.write(0, blockSize);  
                    if(blockSize < frameSize * packSize)
                        break;
                    blockSize = 0;
                }      
            }
        }
    }
    public void fileRead(int i){
        synchronized(lock){
            blockSize = i;
            lock.notify();
        }
    }
    public void addAck(long num){
        synchronized(lock){
            if(num >= currentPos && num < (currentPos + frameSize) && (blockSize != -2))
                acks |= 1 << (num % frameSize); 
        }
    }
    public void stop(){
        if(isRunning){
            isRunning = false;
            t.interrupt();
            nh.stop();
            ack.stop();
            if(fr != null)            
                fr.stop();
            if(fw != null){                
                fw.write(0, 0);
                fw.stop();
            }            
        }
    }
}