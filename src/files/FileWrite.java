package files;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import net.Protocol;

public class FileWrite implements Runnable{
    
    private final FileOutputStream fis;
    private final Protocol p;
    
    private volatile boolean isRunning = false;
    private volatile boolean flag = false;
    private volatile int off, len;    
    private long count = 0;
    private Thread t;
            
    public FileWrite(Protocol pp, String filePath) throws IOException{
        p = pp;
        File f = new File(filePath);
        if(!f.createNewFile()) {
            f.delete();
            f.createNewFile();
        }
        fis = new FileOutputStream(f);
    }
    public void write(int of, int l){     
        while(flag);
        synchronized(this){
            if(of >= 0 && l >= 0){
                off = of;
                len = l;
                flag = true;
                this.notify();   
            }           
        }             
    }
    @Override
    public void run(){         
        isRunning = true;
        t = Thread.currentThread();
        try {                      
            synchronized(this){
                while(isRunning){     
                    this.wait();     
                    if(len > 0){
                        fis.write(p.buffer, off, len);
                        count += (len-off);
                        System.out.println(count);
                    }               
                    flag = false;
                }    
            }
        } catch (IOException | InterruptedException ex) { }
        finally{
            stop();
        }
    }       
    public void stop(){
        if(isRunning){
            isRunning = false;  
            t.interrupt();
            try{
                fis.close();
            }catch(IOException e) {}
        }
    } 
}