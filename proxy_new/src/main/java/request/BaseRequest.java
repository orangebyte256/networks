package main.java.request;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by orangebyte256 on 24.11.15.
 */


abstract public class BaseRequest {
    public static final String LINE_TRANSLATION = "\r\n";
    static final String HEADER_SPLITTER = ": ";
    static final String VERSION_START = "HTTP";
    byte[] rest = null;
    public static boolean isRead(byte[] string)
    {
        String str = new String(string, StandardCharsets.UTF_8);
        return str.indexOf(LINE_TRANSLATION + LINE_TRANSLATION) != -1;
    }
    int findEndHeader(byte[] string)
    {
        String sample = LINE_TRANSLATION + LINE_TRANSLATION;
        for(int i = 0; i < string.length - sample.length(); i++)
        {
            boolean find = true;
            for(int j = 0; j < sample.length(); j++)
            {
                if(string[i + j] != sample.charAt(j))
                    find = false;
            }
            if(find)
                return i;
        }
        return -1;
    }
    public BaseRequest(byte[] string) throws UnknownHostException {
        boolean isBody = false;
        byte[] original = string;
        StringBuilder builder = new StringBuilder();
        String str = new String(string, StandardCharsets.UTF_8);
        int pos = str.indexOf(LINE_TRANSLATION + LINE_TRANSLATION);
        str = str.substring(str.indexOf(LINE_TRANSLATION) + LINE_TRANSLATION.length());
        String[] arr = str.split(LINE_TRANSLATION);
        if(pos != -1)
        {
            rest = Arrays.copyOfRange(original, pos + (LINE_TRANSLATION + LINE_TRANSLATION).length(), original.length);
        }
        for(String s : arr)
        {
            if(s.equals(""))
            {
                break;
            }
            headers.put(s.substring(0, s.indexOf(HEADER_SPLITTER)),
                    s.substring(s.indexOf(HEADER_SPLITTER) + HEADER_SPLITTER.length()));
        }
        headers.put("Connection", "close");
    }
    public void fill(ByteBuffer buffer)
    {
        StringBuilder builder = new StringBuilder();
        for(String s : headers.keySet())
        {
            builder.append(s + HEADER_SPLITTER + headers.get(s) + LINE_TRANSLATION);
        }
        builder.append(LINE_TRANSLATION);
        buffer.put(builder.toString().getBytes());
        if(rest != null)
            buffer.put(rest);
    }
    private Map<String, String> headers = new HashMap<String, String>();
    private String body;
}

