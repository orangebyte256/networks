package client;

import common.DNA;
import common.Message;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by morsk on 10/31/2015.
 */
public class Client {
    private static final int HASH_SIZE = 20;
    private static final char SEPARATOR = '-';
    private static final int MAX_SIZE = 100;
    DataOutputStream output;
    InputStream input;
    String ip = null;
    int port = 0;
    byte[] hash = new byte[HASH_SIZE];
    Socket makeSocket() throws IOException {
        Socket server = new Socket(ip, port);
        output = new DataOutputStream(server.getOutputStream());
        input = server.getInputStream();
        return server;
    }
    Client(String ip, String port) throws IOException
    {
        this.ip = ip;
        this.port = Integer.parseInt(port);
        Socket server = makeSocket();
        output.write(Message.INIT.toByte());
        input.read(hash);
        server.close();
    }
    private boolean isHashEquals(byte[] first, byte[] second)
    {
        for(int i = 0; i < HASH_SIZE; i++)
        {
            if(first[i] != second[i])
                return false;
        }
        return true;
    }
    void work() throws IOException, NoSuchAlgorithmException {
        while(true)
        {
            Socket server = makeSocket();
            byte[] rangeByte = new byte[MAX_SIZE];
            output.write(Message.GET_WORK.toByte());
            input.read(rangeByte);
            server.close();
            int[] range = {0,0};
            int num = 0;
            for(int i = 0; i < rangeByte.length; i++ )
            {
                if(rangeByte[i] == SEPARATOR)
                {
                    num++;
                    continue;
                }
                if(rangeByte[i] == 0)
                {
                    break;
                }
                range[num] *= 10;
                range[num] += Integer.parseInt(String.valueOf(rangeByte[i]));
            }
            server.close();
            for(int i = range[0]; i < range[1]; i++)
            {
                String dna = DNA.getStringDNA(i);
                MessageDigest md = MessageDigest.getInstance("MD5");
                if(isHashEquals(md.digest(dna.getBytes()), hash))
                {
                    Socket socket = makeSocket();
                    output.write(Message.FINDED.toByte());
                    output.write(String.valueOf(i).getBytes());
                    socket.close();
                    return;
                }
            }
            server = makeSocket();
            output.write(Message.CALCULATED.toByte());
            server.close();
        }
    }
}
