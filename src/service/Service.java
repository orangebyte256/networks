package service;

import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by morsk on 10/3/2015.
 */
public class Service {
    static final private int MESSAGE_LENGTH = 256;
    static final private int MAX_ENCODING_SIZE = 256;
    static final private int PORT = 9999;
    static final private int TIME_SEND_ALIVE = 500;
    static final private int TIME_TO_LIVE = 2000;
    static final private String BROADCAST_ADRESS = "255.255.255.255";
    String name;
    private Map<InetAddress, Pair<String, Integer>> map = new HashMap();
    public Service(String name)
    {
        this.name = name;
    }
    private byte[] prepareMessage(String message)
    {
        if(message.getBytes().length < MESSAGE_LENGTH)
            return message.getBytes();
        int size = 128;
        int offset = 128;
        String result = message.substring(0, offset);
        while(size > 1)
        {
            size /= 2;
            if(result.getBytes().length > MESSAGE_LENGTH)
            {
                offset -= size;
            }
            else
            {
                offset += size;
            }
            result = message.substring(0, offset);
        }
        return result.getBytes();
    }

    private final static void clearConsole()
    {
        try
        {
            final String os = System.getProperty("os.name");

            if (os.contains("Windows"))
            {
                Runtime.getRuntime().exec("cls");
            }
            else
            {
                Runtime.getRuntime().exec("clear");
            }
        }
        catch (final Exception e)
        {
            //  Handle any exceptions.
        }
    }
    private void listen() throws IOException {
        DatagramSocket serverSocket = new DatagramSocket(PORT);
        byte[] receiveData = new byte[MESSAGE_LENGTH];
        long tmpTime = TIME_SEND_ALIVE;
        long start;
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);
            serverSocket.setSoTimeout((int) tmpTime);
            start = System.currentTimeMillis() % 1000;
            long end = 0;
            try {
                serverSocket.receive(receivePacket);
                String sentence = new String( receivePacket.getData());
                map.put(receivePacket.getAddress(), new Pair<>(sentence, TIME_TO_LIVE));
                end = System.currentTimeMillis() % 1000;
                tmpTime -= end - start;
                if(tmpTime < 0) tmpTime = 0;
            }
            catch (SocketTimeoutException e) {
                keepAliveSender();
                tmpTime = TIME_SEND_ALIVE;
            }
            updateMap((int) (end - start));
            showNames();
        }
    }

    private void updateMap(int time)
    {
        for ( InetAddress entry : map.keySet())
        {
            Pair<String, Integer> value = map.get(entry);
            value = new Pair<String, Integer>(value.getKey(), value.getValue() - time);
            if(value.getValue() < 0)
            {
                map.remove(value.getKey());
            }
            else
            {
                map.put(entry, value);
            }
        }
    }

    private void showNames()
    {
        clearConsole();
        for ( InetAddress entry : map.keySet())
        {
            System.out.println(map.get(entry).getValue());
        }
    }

    private void keepAliveSender() throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket clientSocket = new DatagramSocket();
        byte[] sendData = prepareMessage(name);
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName(BROADCAST_ADRESS), PORT);
        clientSocket.send(sendPacket);
        clientSocket.close();
    }

    static public void main(String args[])
    {
        if(args.length != 1)
        {
            System.out.println("Error! You should input first argument which will be your username");
            return;
        }
        else
        {
            Service service = new Service(args[0]);
            try {
                service.listen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
