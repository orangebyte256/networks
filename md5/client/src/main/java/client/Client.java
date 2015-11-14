package client;

import common.Message;
import common.Parameters;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by morsk on 10/31/2015.
 */
public class Client {
    DataOutputStream output;
    InputStream input;
    String ip = null;
    int port = 0;
    byte[] hash = new byte[Parameters.HASH_SIZE];

    private void readAll(byte[] res, InputStream input) throws IOException
    {
        int tmp = 0;
        int count = 0;
        while(count != res.length)
        {
            tmp = input.read(res, count, res.length - count);
            if(tmp == -1)
                throw new IOException();
            count += tmp;
        }
    }

    private int readAllUntilEnd(byte[] res, InputStream input) throws IOException {
        int tmp = 0;
        int count = 0;
        while((tmp = input.read(res, count, res.length - count)) > 0)
        {
            count += tmp;
        }
        return count;
    }

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
        output.write(Message.GET_HASH.toByte());
        readAll(hash, input);
        server.close();
    }
    private boolean isHashEquals(byte[] first, byte[] second)
    {
        for(int i = 0; i < Parameters.HASH_SIZE; i++)
        {
            if(first[i] != second[i])
                return false;
        }
        return true;
    }
    private int getNum(byte[] arr)
    {
        String str = new String(arr, StandardCharsets.UTF_8);
        return Integer.parseInt(str);
    }
    void work() throws IOException, NoSuchAlgorithmException {
        while(true)
        {
            try {
              Thread.sleep(1000);
            } catch (InterruptedException ie) {
            }
            Socket server = makeSocket();
            byte[] rangeByte = new byte[Parameters.MAX_SIZE];
            output.write(Message.GET_WORK.toByte());
            int count = readAllUntilEnd(rangeByte, input);
            server.close();
            int num = getNum(Arrays.copyOfRange(rangeByte, 0, count));
            if(num == -1)
                return;
            for(int i = Parameters.BLOCK_SIZE * num; i < Parameters.BLOCK_SIZE * (num + 1); i++)
            {
                String dna = DNA.getStringDNA(i);
                MessageDigest md = MessageDigest.getInstance("MD5");
                if(isHashEquals(md.digest(dna.getBytes()), hash))
                {
                    Socket socket = makeSocket();
                    output.write(Message.FINDED.toByte());
                    output.write(dna.getBytes());
                    socket.close();
                    return;
                }
            }
            server = makeSocket();
            System.err.println(num);
            output.write(Message.CALCULATED.toByte());
            output.write(String.valueOf(num).getBytes());
            server.close();
        }
    }
    public static void main(String []args)
    {
        try {
            Client client = new Client(args[0], args[1]);
            client.work();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
