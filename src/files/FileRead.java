package files;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.Protocol;

public class FileRead implements Runnable{
    
    private final FileInputStream fis;
    private final Protocol p;
    
    private volatile boolean isRunning = false;
    private volatile int off, len;     
    private long count = 0;
    private Thread t;
    private int i = -2;
            
    public FileRead(Protocol pp, String filePath) throws FileNotFoundException{
        p = pp;
        fis = new FileInputStream(filePath);
    }
    public void read(int of, int l){                
        synchronized(this){
            if(of >= 0 && l >= 0){
                off = of;
                len = l;
                this.notify();
            }
        }
    }
    @Override
    public void run(){
        isRunning = true;
        t = Thread.currentThread();
        try{
            synchronized(this){ 
                while(isRunning){     
                    this.wait();
                    if(len > 0)
                    {
                        i = fis.read(p.buffer, off, len);  
                        count += i;
                        System.out.println(count);
                        p.fileRead(i);   
                        if(i == -1){
                            count++;
                            break;
                        }
                    }                
                }     
            }
        } catch (IOException | InterruptedException e) {}
        finally{
            stop();
        }
    }       
    public void stop(){
        if(isRunning){
            isRunning = false;  
            t.interrupt();
            p.fileRead(-3);
            try{                 
                if(fis != null) 
                    fis.close();
            }catch(IOException e) {}
        }
    } 
}