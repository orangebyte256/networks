package Client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by morsk on 10/28/2015.
 */


public class Sender {
    static private final char sizeEnd = '.';
    static byte[] makeLengthMessage(int count)
    {
        String res = String.valueOf(count);
        res = res + String.valueOf(sizeEnd);
        return res.getBytes();
    }
    static void send(Socket socket, byte[] name, byte[] data)
    {
        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.write(makeLengthMessage(name.length));
            out.write(name);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
