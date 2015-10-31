package Server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Created by morsk on 10/29/2015.
 */
public class Parser {
    static private final char sizeEnd = '.';
    static public String readName(InputStream in)
    {
        int num = 0;
        byte[] tmp = new byte[1];
        do {
            num *= 10;
            try {
                in.read(tmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            num += Integer.valueOf(tmp[0]);
        } while(tmp[0] != sizeEnd);
        tmp = new byte[num];
        try {
            in.read(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(tmp, StandardCharsets.UTF_8);
    }
}
