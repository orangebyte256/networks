package main.java.request;

import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * Created by orangebyte256 on 24.11.15.
 */
public class AnswerRequest extends BaseRequest {
    public AnswerRequest(byte[] bytes) throws UnknownHostException {
        super(bytes);
        String string = new String(bytes, StandardCharsets.UTF_8);
        String[] mainLine = string.substring(0, string.indexOf(LINE_TRANSLATION)).split(" ");
        version = mainLine[0].substring(mainLine[0].indexOf(VERSION_START) + VERSION_START.length() + 1);
        String first = string.substring(0, string.indexOf(LINE_TRANSLATION));
        rest = first.substring(first.indexOf(LINE_TRANSLATION) + LINE_TRANSLATION.length() + 1);
        version = "1.0";
    }
@Override
    public void fill(ByteBuffer buffer)
    {
        String temp = VERSION_START + "/" + version + " " + rest + LINE_TRANSLATION;
        buffer.put(temp.getBytes());
        super.fill(buffer);
    }
    public static String make(int num, String text)
    {
        return VERSION_START + "/" + "1.0" + " " + new Integer(num).toString() + " " + text + LINE_TRANSLATION + LINE_TRANSLATION;
    }
    private String version;
    private String rest;
}
