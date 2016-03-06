package ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.Protocol;

public class Test {
    
    public static void main(String[] args) {
        try {
            (new Thread(new Protocol(Params.FRAME_SIZE, Params.PACK_SIZE, true, InetAddress./*getByName("192.168.0.102")*/getLocalHost(), Params.DATA_PORT, Params.ACK_PORT, "I:\\Images\\AnonOS_x32_0.1.iso"))).start();
        } catch (UnknownHostException ex) {}
    }
}
