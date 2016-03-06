
package ui;

import java.net.InetAddress;
import java.net.UnknownHostException;
import net.Protocol;

public class Client {
    public static void main(String[] args) {
        try {
            (new Thread(new Protocol(Params.FRAME_SIZE, Params.PACK_SIZE, false, InetAddress.getByName("192.168.0.102"), Params.DATA_PORT, Params.ACK_PORT, "11.iso"))).start();
        } catch (UnknownHostException | IllegalStateException e) {
            
        }
    }
}
