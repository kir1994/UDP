package ui;

public class Params {
    
    public static int ACK_PORT = 1489;
    public static int DATA_PORT = 1488;
    public static int BYTES_IN_KIB = 1024;
    public static int FRAME_SIZE = 30;
    public static int PACK_SIZE = 16;
    public static int BYTE_MASK = 0xFF;
    public static int LONG_BYTE_SIZE = 8;

    public static byte[] int2Byte(long i, byte[] b, int off) {
        if (b.length - off >= Params.LONG_BYTE_SIZE) {
            for (int j = 0; j < Params.LONG_BYTE_SIZE; ++j)
                b[off + j] = (byte) (i >> (Params.LONG_BYTE_SIZE * j));
            return b;
        } else
            throw new IllegalArgumentException();
    }
    public static long byte2Int(byte[] b, int off) {
        if (b.length - off >= Params.LONG_BYTE_SIZE) {
            long i = 0;
            for (int j = 0; j < Params.LONG_BYTE_SIZE; ++j)
                i |= (b[j + off] & Params.BYTE_MASK) << (Params.LONG_BYTE_SIZE * j);
            return i;
        } else
            throw new IllegalArgumentException();
    }    
}
