package main.java.request;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

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

public class BaseRequest {
    public static final String LINE_TRANSLATION = "\r\n";
    static final String HEADER_SPLITTER = ": ";
    static final String VERSION_START = "HTTP";
    public BaseRequest(String string) throws UnknownHostException {
        boolean isBody = false;
        StringBuilder builder = new StringBuilder();
        String[] arr = string.split(LINE_TRANSLATION);
        for(String s : arr)
        {
            if(!isBody)
            {
                if(s.equals(""))
                {
                    isBody = true;
                    continue;
                }
                headers.put(s.substring(0, s.indexOf(HEADER_SPLITTER)),
                        s.substring(s.indexOf(HEADER_SPLITTER) + HEADER_SPLITTER.length()));
            }
            else
            {
                builder.append(s + LINE_TRANSLATION);
            }
        }
        body = builder.toString();
        headers.put("Connection", "close");
    }
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for(String s : headers.keySet())
        {
            builder.append(s + HEADER_SPLITTER + headers.get(s));
        }
        return builder.toString();
    }
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;
}

