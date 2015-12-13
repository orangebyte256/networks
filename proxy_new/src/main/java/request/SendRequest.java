package main.java.request;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by orangebyte256 on 24.11.15.
 */

class Adress
{
    static final String SPLITTER = "://";
    public Adress(String s) throws UnknownHostException {
        protocol = s.substring(0, s.indexOf(SPLITTER));
        String host = s.substring(s.indexOf(SPLITTER) + SPLITTER.length(),
                s.indexOf("/", s.indexOf(SPLITTER) + SPLITTER.length()));
        adrees = InetAddress.getByName(host);
        rest = s.substring(s.indexOf("/", s.indexOf(SPLITTER) + SPLITTER.length()));
    }
    public String toString()
    {
        return protocol + SPLITTER + adrees.getHostName() + "/" + rest;
    }
    String protocol;
    InetAddress adrees;
    String rest;
}

public class SendRequest extends BaseRequest {
    private static final String[] VALID_METHODS = {"GET", "POST", "HEAD"};
    private static final String VALID_PROTOCOL = "http";
    private static final int STANDART_PORT = 80;
    public SocketAddress getAdress()
    {
        return new InetSocketAddress(adress.adrees, STANDART_PORT);
    }
    public SendRequest(byte[] bytes) throws UnknownHostException {
        super(bytes);
        String string = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(string);
        String[] mainLine = string.substring(0, string.indexOf(BaseRequest.LINE_TRANSLATION)).split(" ");
        method = mainLine[0];
        adress = new Adress(mainLine[1]);
        version = mainLine[2].substring(mainLine[2].indexOf(VERSION_START) + VERSION_START.length() + 1);                                 //!!! can be space inside query
        version = "1.0";
    }
    public boolean isValid()
    {
        if(!adress.protocol.equals(VALID_PROTOCOL))
            return false;
        for(String s : VALID_METHODS)
        {
            if(s.equals(method))
                return true;
        }
        return false;
    }
    @Override
    public void fill(ByteBuffer buffer)
    {
        String temp = method + " " + adress.rest + " " + VERSION_START + "/" + version + LINE_TRANSLATION;
        buffer.put(temp.getBytes());
        super.fill(buffer);
    }
    private String method;
    private Adress adress;
    private String version;
}
